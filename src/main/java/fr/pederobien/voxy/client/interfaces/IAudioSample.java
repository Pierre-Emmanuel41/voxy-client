package fr.pederobien.voxy.client.interfaces;

public interface IAudioSample {

	/**
	 * @return The player's name associated to this audio sample.
	 */
	String getName();

	/**
	 * @return The bytes array that contains the audio sample.
	 */
	byte[] getData();

	/**
	 * @return The volume on the left side.
	 */
	float getLeft();

	/**
	 * @return The volume on the right side.
	 */
	float getRight();

	/**
	 * @return The global volume on both sides.
	 */
	float getGlobal();
}
