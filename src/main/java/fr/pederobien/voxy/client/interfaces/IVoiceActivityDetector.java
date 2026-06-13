package fr.pederobien.voxy.client.interfaces;

public interface IVoiceActivityDetector {

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
	 */
	void reset();
}
