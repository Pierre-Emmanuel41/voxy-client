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
	 * @param name The name of the audio stream to update
	 * @param data The next audio sample of the audio stream.
	 */
	public void write(String name, byte[] data);
}
