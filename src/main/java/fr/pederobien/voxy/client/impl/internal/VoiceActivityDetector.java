package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.interfaces.IVoiceActivityDetector;

public class VoiceActivityDetector implements IVoiceActivityDetector {
	// Hysteresis Factors (Applied to the adaptive threshold)
	private static final double START_HYSTERESIS_FACTOR = 1.2; // Start if > 1.2 * threshold
	private static final double STOP_HYSTERESIS_FACTOR = 0.8; // Stop if < 0.8 * threshold
	private static final double ZCR_NOISE_THRESHOLD = 0.15;

	private static final int WARMUP_FRAMES = 10;
	private static final double NOISE_MULTIPLIER = 4;
	private static final double MAX_NOISE_RMS = 2000.0;

	private static final long TAIL_DURATION_MS = 500;

	private boolean isSending;
	private long silenceStartTime;
	private boolean isWarmup;
	private int quietFrameCount;
	private double noiseFloorRMS;
	private double noiseFloorZCR;
	private double currentEnergyThreshold;

	public VoiceActivityDetector() {
		isSending = false;
		silenceStartTime = 0;
		isWarmup = true;
		quietFrameCount = 0;
		noiseFloorRMS = Double.MAX_VALUE;
		noiseFloorZCR = 0.0;
		currentEnergyThreshold = 0.0;
	}

	@Override
	public boolean checkVoiceActivity(byte[] audioFrame) {
		if (audioFrame.length == 0)
			return false;

		int samplesCount = audioFrame.length / 2;
		long sumSquared = 0;
		int crossings = 0;
		short prevSample = 0;

		for (int i = 0; i < samplesCount; i++) {
			short sample = (short) ((audioFrame[2 * i + 1] & 0xFF) << 8 | (audioFrame[2 * i] & 0xFF));
			sumSquared += (long) sample * sample;
			if (i > 0 && ((prevSample > 0 && sample <= 0) || (prevSample < 0 && sample >= 0))) {
				crossings++;
			}
			prevSample = sample;
		}

		double rms = Math.sqrt((double) sumSquared / samplesCount);
		double zcr = (double) crossings / samplesCount;

		// Step 1: Warmup Phase
		if (isWarmup) {
			if (rms < MAX_NOISE_RMS) {
				updateNoiseFloor(rms, zcr);
				quietFrameCount++;
			}
			if (quietFrameCount >= WARMUP_FRAMES) {
				isWarmup = false;
				currentEnergyThreshold = noiseFloorRMS * NOISE_MULTIPLIER;

				Logger.debug(3, "[VoiceActivityDetector] - Warmup phase over");
			}

			// During warm-up phase, nothing to send
			return false;
		}

		// Step 2: VAD Logic with Adaptive Hysteresis
		double dynamicStartThreshold = currentEnergyThreshold * START_HYSTERESIS_FACTOR;
		double dynamicStopThreshold = currentEnergyThreshold * STOP_HYSTERESIS_FACTOR;

		if (zcr > ZCR_NOISE_THRESHOLD) {
			// High ZCR = Noise
			if (isSending) {
				if (silenceStartTime == 0)
					silenceStartTime = System.currentTimeMillis();
				if (System.currentTimeMillis() - silenceStartTime > TAIL_DURATION_MS) {
					isSending = false;
					silenceStartTime = 0;
				}
				return true; // Keep sending during hangover
			}
			return false;
		}

		if (!isSending) {
			if (rms > dynamicStartThreshold) {
				isSending = true;
				silenceStartTime = 0;
				return true;
			}
			return false;
		} else {
			if (rms < dynamicStopThreshold) {
				if (silenceStartTime == 0)
					silenceStartTime = System.currentTimeMillis();
				if (System.currentTimeMillis() - silenceStartTime > TAIL_DURATION_MS) {
					isSending = false;
					silenceStartTime = 0;
					return false;
				}
				return true; // Keep sending during hangover
			} else {
				silenceStartTime = 0;
				return true;
			}
		}
	}

	@Override
	public void decreaseSensitivity() {
		currentEnergyThreshold -= 0.2;
	}

	@Override
	public void increaseSensitivity() {
		currentEnergyThreshold += 0.2;
	}

	private void updateNoiseFloor(double rms, double zcr) {
		if (rms < noiseFloorRMS) {
			noiseFloorRMS = rms;
		}
		noiseFloorZCR = ((noiseFloorZCR * quietFrameCount) + zcr) / (quietFrameCount + 1);
	}

	public void reset() {
		isWarmup = true;
		quietFrameCount = 0;
		noiseFloorRMS = Double.MAX_VALUE;
		noiseFloorZCR = 0.0;
		currentEnergyThreshold = 0.0;
		isSending = false;
		silenceStartTime = 0;
	}
}