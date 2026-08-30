package fr.pederobien.voxy.client.interfaces;

import java.util.Map;

import fr.pederobien.sound.interfaces.IEffect;
import fr.pederobien.sound.interfaces.IEffectParametersHolder;
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
	 * @param values A map that gather parameter's name / parameters's value.
	 * @return The created effect if registered, null otherwise.
	 */
	IEffect createEffect(String name, Map<String, Object> values);

	/**
	 * Creates a holder that contains effect parameters updated with the given values map.
	 * 
	 * @param name   The name of the effect.
	 * @param values A map that gather parameter's name / parameter's value.
	 * @return The parameters holder if registered, null otherwise.
	 */
	IEffectParametersHolder createHolder(String name, Map<String, Object> values);
}
