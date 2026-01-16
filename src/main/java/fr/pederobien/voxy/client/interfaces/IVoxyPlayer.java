package fr.pederobien.voxy.client.interfaces;

public interface IVoxyPlayer {

	/**
	 * @return The player's name
	 */
	String getName();

	/**
	 * @return True if the player is muted, false otherwise.
	 */
	boolean isMute();

	/**
	 * Mute this player only for this client.
	 *
	 * @param isMute True to mute the player, false to unmute.
	 */
	void setMute(boolean isMute);

	/**
	 * @return True if this player is muted by the main player, false otherwise.
	 */
	boolean isMuteByMainPlayer();

	/**
	 * @return True if the player disabled it speakers, false otherwise.
	 */
	boolean isDeaf();
}
