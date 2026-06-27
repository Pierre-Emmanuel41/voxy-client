package fr.pederobien.voxy.client.impl.internal;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.interfaces.IVoiceActivityDetector;

public class VoiceActivityDetector implements IVoiceActivityDetector {
	private State notInitialized;
	private State warmup;
	private State warmupOver;
	private State current;
	private double rmsSensitivityFactor;
	private double rmsThresholdStart;
	private double rmsThresholdStop;
	private double zcrSensitivityFactor;
	private double zcrThreshold;

	private double rmsAverage;
	private double rmsDeviation;
	private double zcrAverage;
	private double zcrDeviation;

	private short prevSample;

	/**
	 * Creates a voice activity detector to reduce as possible the sending of background noise to the server.
	 * 
	 * @param sampleRate   The sample rate used by the microphone.
	 * @param warmupTime   The time, in ms, for the warmup sequence. Thresholds are computed during that time.
	 * @param hangoverTime The time, in ms, to let silence samples being sent after silence is detected.
	 */
	public VoiceActivityDetector() {
		notInitialized = new NotInitialized();
		current = notInitialized;

		rmsSensitivityFactor = 2.5;
		zcrSensitivityFactor = 1.5;
	}

	@Override
	public void initialize(float sampleRate, int warmupTime, int hangoverTime) {
		current.initialize(sampleRate, warmupTime, hangoverTime);
	}

	@Override
	public boolean checkVoiceActivity(byte[] buffer) {
		return current.checkVoiceActivity(buffer);
	}

	@Override
	public void decreaseSensitivity() {
		current.decreaseSensitivity();
	}

	@Override
	public void increaseSensitivity() {
		current.increaseSensitivity();
	}

	@Override
	public void reset(boolean fullReset) {
		current.reset(fullReset);
	}

	private static double fastSqrt(double number) {
		// Bitwise approximation for speed
		double sqrt = Double.longBitsToDouble(((Double.doubleToLongBits(number) - (1l << 52)) >> 1) + (1l << 61));

		// One Newton-Raphson iteration to improve accuracy
		return (sqrt + number / sqrt) / 2.0;
	}

	private abstract class State {

		/**
		 * Initialize this voice activity detector to reduce as possible the sending of background noise to the server.
		 * 
		 * @param sampleRate   The sample rate used by the microphone.
		 * @param warmupTime   The time, in ms, for the warmup sequence. Thresholds are computed during that time.
		 * @param hangoverTime The time, in ms, to let silence samples being sent after silence is detected.
		 */
		protected abstract void initialize(float sampleRate, int warmupTime, int hangoverTime);

		/**
		 * Check if the buffer contains player's voice or not.
		 * 
		 * @param buffer The buffer that contains already filtered microphone's audio sample.
		 * @return True if the buffer shall be sent to the server, false if it shall be dropped.
		 */
		protected abstract boolean checkVoiceActivity(byte[] buffer);

		/**
		 * Increase slightly the sensitivity of this detector. If too sensitive, the background noise may be detected as player's voice.
		 */
		protected abstract void increaseSensitivity();

		/**
		 * Decrease slightly the sensitivity of this detector. If not enough sensitive, the player will have to speak louder to be
		 * detected.
		 */
		protected abstract void decreaseSensitivity();

		/**
		 * Reset internal variable so that the next call to checkVoiceActivity has the same result as it was the first time it was called.
		 * 
		 * @param fullReset Indicates if the reset shall also reinitialize user specific sensitivity (modified via increase/decrease
		 *                  sensitivity methods) and the initialize method shall be call prior calling checkVoiceActivity method.
		 */
		protected abstract void reset(boolean fullReset);

		/**
		 * Compute the rmsThresholdStart/Stop and zcrThreshold based on rms/zcr average/deviation and sensitivity.
		 */
		protected void computeThresholds() {
			rmsThresholdStart = rmsAverage + (rmsSensitivityFactor * rmsDeviation);
			rmsThresholdStop = rmsAverage + ((rmsSensitivityFactor - 1.0) * rmsDeviation); // Hysteresis
			zcrThreshold = zcrAverage + (zcrSensitivityFactor * zcrDeviation);

			rmsThresholdStart = Math.max(0.001, rmsThresholdStart);

			String format = "Thresholds: rmsStart=%s, rmsStop=%s, zcr=%s";
			debug(format, rmsThresholdStart, rmsThresholdStop, zcrThreshold);
		}

		/**
		 * Display a debug log with log level 3.
		 * 
		 * @param format The formatter if the message to display has arguments.
		 * @param args   The arguments of the message to display.
		 */
		protected void debug(String format, Object... args) {
			Logger.debug(3, "[VoiceActivityDetector] - %s", String.format(format, args));
		}

		/**
		 * Display a debug log with log level 3.
		 * 
		 * @param format The formatter if the message to display has arguments.
		 * @param args   The arguments of the message to display.
		 */
		protected void info(String format, Object... args) {
			Logger.info("[VoiceActivityDetector] - %s", String.format(format, args));
		}
	}

	private class NotInitialized extends State {

		@Override
		protected void initialize(float sampleRate, int warmupTime, int hangoverTime) {
			warmup = new WarmupState(sampleRate, warmupTime);
			warmupOver = new WarmupOverState(hangoverTime);
			current = warmup;
		}

		@Override
		protected boolean checkVoiceActivity(byte[] buffer) {
			debug("Not initialized, call initialize method prior checking voice");
			return false;
		}

		@Override
		protected void increaseSensitivity() {
			// Do nothing
		}

		@Override
		protected void decreaseSensitivity() {
			// Do nothing
		}

		@Override
		protected void reset(boolean fullReset) {
			// Do nothing
		}
	}

	private class WarmupState extends State {
		private final float sampleRate;
		private final int warmupTime;
		private final List<Double> rmsList;
		private final List<Double> zcrList;
		private int time;
		private boolean initialized;

		public WarmupState(float sampleRate, int warmupTime) {
			this.sampleRate = sampleRate;
			this.warmupTime = warmupTime;

			rmsList = new ArrayList<Double>();
			zcrList = new ArrayList<Double>();
			time = 0;
			rmsAverage = 0;
			rmsDeviation = 0;
			zcrAverage = 0;
			zcrDeviation = 0;
			initialized = false;
			prevSample = 0;
		}

		@Override
		protected void initialize(float sampleRate, int warmupTime, int hangoverTime) {
			throw new IllegalStateException("Cannot call initialize in Warm-Up state");
		}

		@Override
		public boolean checkVoiceActivity(byte[] buffer) {
			if (time == 0)
				info("Starting warm-up phase, please do not speak");

			// Initialization variables
			int samplesCount = buffer.length / 2;
			long sumSquared = 0;
			int crossings = 0;

			if (!initialized) {
				prevSample = (short) ((buffer[1] & 0xFF) << 8 | (buffer[0] & 0xFF));
				initialized = true;
			}

			for (int i = 0; i < samplesCount; i++) {
				short sample = (short) ((buffer[2 * i + 1] & 0xFF) << 8 | (buffer[2 * i] & 0xFF));
				sumSquared += (long) sample * sample;

				if ((prevSample > 0 && sample <= 0) || (prevSample < 0 && sample >= 0))
					crossings++;

				prevSample = sample;
			}

			// Storing computed rms and zcr values for analysis when warm-up is over
			rmsList.add(fastSqrt(sumSquared / (double) samplesCount));
			zcrList.add(crossings / (double) samplesCount);

			// Updating current time
			time += (int) ((buffer.length / 2.0f) / sampleRate * 1000.0f);
			if (warmupTime <= time) {

				// Computing rms and zcr average and deviation
				computeAverageAndDeviation(rmsList, zcrList);

				// Computing thresholds
				computeThresholds();

				info("Warmup phase over, you can speak now");

				// Updating state
				current = warmupOver;
			}

			// During warm-up, no data shall be sent
			return false;
		}

		@Override
		public void increaseSensitivity() {
			// Do nothing
		}

		@Override
		public void decreaseSensitivity() {
			// Do nothing
		}

		@Override
		public void reset(boolean fullReset) {
			rmsList.clear();
			zcrList.clear();
			rmsAverage = 0;
			rmsDeviation = 0;
			zcrAverage = 0;
			zcrDeviation = 0;
			time = 0;
			initialized = false;
			prevSample = 0;

			if (fullReset) {
				rmsSensitivityFactor = 2.5;
				zcrSensitivityFactor = 1.5;

				current = notInitialized;
			}
		}

		/**
		 * Compute the mean (average) on a dataset.
		 * 
		 * @param values The list of values.
		 */
		private double computeAverage(List<Double> values) {
			if (values.isEmpty())
				return 0.0;

			double sum = 0.0;
			for (double value : values)
				sum += value;

			return sum / values.size();
		}

		/**
		 * Computes the standard deviation of a dataset.
		 * 
		 * @param values The list of values.
		 * @param mean   The mean of the dataset.
		 * @return The standard deviation.
		 */
		private double computeStandardDeviation(List<Double> values, double mean) {
			if (values.isEmpty())
				return 0.0;

			double sum = 0.0;
			for (double val : values) {
				sum += Math.pow(val - mean, 2);
			}
			return fastSqrt(sum / values.size());
		}

		/**
		 * Computes rms and zcr average and deviation based on the given dataset.
		 * 
		 * @param rmsList A list that contains rms dataset during the warmup phase.
		 * @param zcrList A list that contains zcr dataset during the warmup phase.
		 */
		private void computeAverageAndDeviation(List<Double> rmsList, List<Double> zcrList) {
			rmsAverage = computeAverage(rmsList);
			rmsDeviation = computeStandardDeviation(rmsList, rmsAverage);

			zcrAverage = computeAverage(zcrList);
			zcrDeviation = computeStandardDeviation(zcrList, zcrAverage);
		}
	}

	private class WarmupOverState extends State {
		private final int hangoverTime;
		private long silenceStartTime;
		private boolean isSpeechActive;

		/**
		 * Creates a voice activity detector with computed thresholds.
		 */
		public WarmupOverState(int hangoverTime) {
			this.hangoverTime = hangoverTime;

			silenceStartTime = 0;
			isSpeechActive = false;
		}

		@Override
		protected void initialize(float sampleRate, int warmupTime, int hangoverTime) {
			throw new IllegalStateException("Cannot call initialize in Warm-Up over state");
		}

		@Override
		public boolean checkVoiceActivity(byte[] buffer) {
			// Initialization variables
			int samplesCount = buffer.length / 2;
			long sumSquared = 0;
			int crossings = 0;

			for (int i = 0; i < samplesCount; i++) {
				short sample = (short) ((buffer[2 * i + 1] & 0xFF) << 8 | (buffer[2 * i] & 0xFF));
				sumSquared += (long) sample * sample;

				if ((prevSample > 0 && sample <= 0) || (prevSample < 0 && sample >= 0))
					crossings++;

				prevSample = sample;
			}

			// Computing rms and zcr values to detect voice
			double rms = fastSqrt(sumSquared / (double) samplesCount);
			double zcr = crossings / (double) samplesCount;

			// Detection Logic
			boolean isHighEnergy = rms > (isSpeechActive ? rmsThresholdStop : rmsThresholdStart);
			boolean isLowZCR = zcr < zcrThreshold;
			boolean isVoiceCandidate = isHighEnergy && isLowZCR;

			long now = System.currentTimeMillis();

			if (isVoiceCandidate) {
				// Voice detected: Reset timer and activate
				silenceStartTime = 0;
				isSpeechActive = true;
				return true;
			} else {
				// Silence detected
				if (isSpeechActive) {
					if (silenceStartTime == 0) {
						silenceStartTime = now; // Start hangover timer
					}

					if ((now - silenceStartTime) >= hangoverTime) {
						// Hangover expired -> Switch to silence
						isSpeechActive = false;
						silenceStartTime = 0;
						return false;
					} else {
						// Still in hangover period -> Treat as voice
						return true;
					}
				} else {
					// Already silence
					return false;
				}
			}
		}

		@Override
		public void increaseSensitivity() {
			// RMS: Lower threshold to catch quieter sounds
			rmsSensitivityFactor = Math.max(0.5, rmsSensitivityFactor - 0.5);

			// ZCR: RAISE threshold to allow noisier speech (higher ZCR) to pass
			// Because condition is (zcr < threshold), a higher threshold is more permissive.
			zcrSensitivityFactor = Math.min(10.0, zcrSensitivityFactor + 0.5);

			computeThresholds();
		}

		@Override
		public void decreaseSensitivity() {
			// RMS: Raise threshold to ignore quiet noise
			rmsSensitivityFactor = Math.min(10.0, rmsSensitivityFactor + 0.5);

			// ZCR: LOWER threshold to reject noisy speech (only accept very tonal voice)
			zcrSensitivityFactor = Math.max(0.5, zcrSensitivityFactor - 0.5);

			computeThresholds();
		}

		@Override
		public void reset(boolean fullReset) {
			if (warmup != null)
				warmup.reset(fullReset);

			silenceStartTime = 0;
			isSpeechActive = false;

			// If warmup became notInitialized, we shouldn't switch to warmup.
			// The state switch already happened inside warmup.reset() if fullReset=true.
			if (!fullReset && warmup != null)
				current = warmup;
		}
	}
}