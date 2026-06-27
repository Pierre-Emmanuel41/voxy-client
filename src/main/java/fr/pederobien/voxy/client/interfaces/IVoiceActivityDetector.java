package fr.pederobien.voxy.client.interfaces;

public interface IVoiceActivityDetector {

	/**
	 * Initialize this voice activity detector to reduce as possible the sending of background noise to the server.
	 * 
	 * @param sampleRate   The sample rate used by the microphone.
	 * @param warmupTime   The time, in ms, for the warmup sequence. Thresholds are computed during that time.
	 * @param hangoverTime The time, in ms, to let silence samples being sent after silence is detected.
	 */
	void initialize(float sampleRate, int warmupTime, int hangoverTime);

	/**
	 * Check if the buffer contains player's voice or not.
	 * 
	 * @param buffer The buffer that contains already filtered microphone's audio sample.
	 * @return True if the buffer shall be sent to the server, false if it shall be dropped.
	 */
	boolean checkVoiceActivity(byte[] buffer);

	/**
	 * Increase slightly the sensitivity of this detector. If too sensitive, the background noise may be detected as player's voice.
	 */
	void increaseSensitivity();

	/**
	 * Decrease slightly the sensitivity of this detector. If not enough sensitive, the player will have to speak louder to be
	 * detected.
	 */
	void decreaseSensitivity();

	/**
	 * Reset internal variable so that the next call to checkVoiceActivity has the same result as it was the first time it was called.
	 * 
	 * @param fullReset Indicates if the reset shall also reinitialize user specific sensitivity (modified via increase/decrease
	 *                  sensitivity methods) and the initialize method shall be call prior calling checkVoiceActivity method.
	 */
	void reset(boolean fullReset);
}
