package fr.pederobien.voxy.client.interfaces;

import java.util.function.Supplier;

import fr.pederobien.sound.interfaces.IEffect;
import fr.pederobien.sound.interfaces.ISoundApi;

public interface IVoxyClientConfig {

	/**
	 * @return The player's name.
	 */
	String getName();

	/**
	 * @return The configuration to use for the TCP client.
	 */
	IVoxyTcpConfig getTcpConfig();

	/**
	 * @return The configuration to use for the UDP client.
	 */
	IVoxyUdpConfig getUdpConfig();

	/**
	 * @return The API to use to access the microphone and the speakers.
	 */
	ISoundApi getSoundApi();

	/**
	 * @return The voice activity detector to filter microphone's audio stream.
	 */
	IVoiceActivityDetector getVoiceActivityDetector();

	/**
	 * Registers an algorithm to compress / decompress raw microphone's audio sample.
	 * 
	 * @param algorithm  The algorithm number associated to the supplier.
	 * @param compressor An algorithm to compress / decompress audio samples.
	 * @return True if the algorithm has been registered successfully.
	 */
	boolean registerCompressor(int algorithm, Supplier<ISampleCompressor> compressor);

	/**
	 * @return The compression algorithm to use to compress / decompress microphone's raw audio sample.
	 */
	int getCompressionAlgorithm();

	/**
	 * Get the algorithm associated to the given algorithm number.
	 * 
	 * @param algorithm The key used to find the algorithm.
	 * @return The compressor associated to the given algorithm number if it exists, the default compressor otherwise.
	 */
	ISampleCompressor getCompressor(int algorithm);

	/**
	 * Creates an effect associated to the given name and parameters values.
	 * 
	 * @param name   The name of the effect the creates
	 * @param values The values of effect parameters.
	 * @return The created effect if registered, null otherwise.
	 */
	IEffect createEffect(String name, Object... values);
}
