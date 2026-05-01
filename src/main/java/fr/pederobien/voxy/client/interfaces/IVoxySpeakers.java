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
	void write(String name, byte[] data);

	/**
	 * Set the left, right and global volumes of an audio stream.
	 * 
	 * @param name   The name of the stream.
	 * @param left   The volume on the left side.
	 * @param right  The volume on the right side.
	 * @param global The global volume on both sides.
	 */
	void setVolumes(String name, float left, float right, float global);

	/**
	 * Set to 1.0 the left, right and global volumes of each registered stream.
	 */
	void resetVolumes();
}
