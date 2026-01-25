package fr.pederobien.voxy.client.interfaces;

public interface IVoxySpeakers {

	/**
	 * Opens the access to the speakers. If an error occurred while accessing to the speakers, and exception shall be thrown.
	 */
	void open() throws Exception;

	/**
	 * Closes the access to the speakers. If an error occurred while closing the access to the speakers, an exception shall be thrown.
	 */
	void close() throws Exception;

	/**
	 * Write an audio sample in the stream of a player.
	 * 
	 * @param sample The audio sample to play.
	 */
	public void write(IAudioSample sample);
}
