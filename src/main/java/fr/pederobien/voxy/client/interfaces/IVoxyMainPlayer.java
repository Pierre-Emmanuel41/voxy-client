package fr.pederobien.voxy.client.interfaces;

public interface IVoxyMainPlayer {

	/**
	 * @return The player's name.
	 */
	String getName();

	/**
	 * @return True if the player is muted, false otherwise.
	 */
	boolean isMute();

	/**
	 * First sends a request to the server to notify the player's microphone status has changed then throws
	 * VoxyMainPlayerMuteStatusChangedEvent to acknowledge the new mute status.
	 *
	 * @param isMute True to enable, false to disable.
	 */
	void setMute(boolean isMute);

	/**
	 * @return True if the player disabled it speakers, false otherwise.
	 */
	boolean isDeaf();

	/**
	 * First sends a request to the server to notify the player's speaker status has changed then throws
	 * VoxyMainPlayerDeafStatusChangedEvent to acknowledge the new deaf status.
	 * 
	 * @param isDeaf True to enable, false to disable.
	 */
	void setDeaf(boolean isDeaf);
}
