package fr.pederobien.voxy.client.impl.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.sound.impl.SoundApi;
import fr.pederobien.sound.interfaces.ISoundApi;
import fr.pederobien.voxy.client.impl.compressors.GZipCompression;
import fr.pederobien.voxy.client.impl.compressors.NoCompression;
import fr.pederobien.voxy.client.impl.internal.VoiceActivityDetector;
import fr.pederobien.voxy.client.interfaces.ISampleCompressor;
import fr.pederobien.voxy.client.interfaces.IVoiceActivityDetector;
import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;

public class VoxyClientConfig implements IVoxyClientConfig {
	private final String name;
	private final VoxyTcpConfig tcpConfig;
	private final VoxyUdpConfig udpConfig;
	private ISoundApi soundApi;
	private IVoiceActivityDetector vad;
	private Map<Integer, ISampleCompressor> compressors;
	private int algorithm;

	/**
	 * Creates a configuration for a voxy client.
	 * 
	 * @param name     The name of the player.
	 * @param endPoint The server's end-point (IP address + port number);
	 */
	public VoxyClientConfig(String name, IEthernetEndPoint endPoint) {
		this.name = name;

		tcpConfig = new VoxyTcpConfig(endPoint);
		udpConfig = new VoxyUdpConfig();
		soundApi = new SoundApi();
		vad = new VoiceActivityDetector();

		compressors = new HashMap<Integer, ISampleCompressor>();
		compressors.put(0, new NoCompression());
		compressors.put(1, new GZipCompression());

		algorithm = 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public VoxyTcpConfig getTcpConfig() {
		return tcpConfig;
	}

	@Override
	public VoxyUdpConfig getUdpConfig() {
		return udpConfig;
	}

	@Override
	public ISoundApi getSoundApi() {
		return soundApi;
	}

	/**
	 * Set the sound API to use to access the player's microphone and speakers.
	 * 
	 * @param soundApi The sound API to use.
	 */
	public void setSoundApi(ISoundApi soundApi) {
		this.soundApi = soundApi;
	}

	@Override
	public IVoiceActivityDetector getVoiceActivityDetector() {
		return vad;
	}

	/**
	 * Set the voice activity detector to use to filter microphone's audio stream and send only player's voice.
	 * 
	 * @param vad The voice activity detector to use.
	 */
	public void setVoiceActivityDetector(IVoiceActivityDetector vad) {
		this.vad = vad;
	}

	@Override
	public boolean registerCompressor(ISampleCompressor compressor) {
		// First 20 values are reserved.
		if (compressor.getAlgorithm() < 20 || 255 < compressor.getAlgorithm())
			return false;

		// An algorithm is already registered for the value.
		if (compressors.containsKey(compressor.getAlgorithm()))
			return false;

		compressors.put(compressor.getAlgorithm(), compressor);
		return true;
	}

	@Override
	public int getCompressionAlgorithm() {
		return algorithm;
	}

	/**
	 * Set the compression algorithm to use to compress microphone's raw audio sample.
	 * 
	 * @param algorithm The algorithm to use to compress samples.
	 * @return True if the value is associated to an existing compression algorithm, false otherwise.
	 */
	public boolean setCompressionAlgorithm(int algorithm) {
		if (!compressors.containsKey(algorithm))
			return false;

		this.algorithm = algorithm;
		return true;
	}

	@Override
	public Optional<ISampleCompressor> getCompressor(int algorithm) {
		return Optional.ofNullable(compressors.get(algorithm));
	}
}
