package fr.pederobien.voxy.client.impl.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.sound.event.SoundApiInitializationErrorEvent;
import fr.pederobien.sound.event.SoundApiInitializedEvent;
import fr.pederobien.sound.impl.SoundApi;
import fr.pederobien.sound.interfaces.ISoundApi;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.impl.compressors.GZipCompression;
import fr.pederobien.voxy.client.impl.compressors.NoCompression;
import fr.pederobien.voxy.client.impl.compressors.OpusCompression;
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
	private Map<Integer, Supplier<ISampleCompressor>> compressors;
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

		compressors = new HashMap<Integer, Supplier<ISampleCompressor>>();
		compressors.put(0, () -> new NoCompression());
		compressors.put(1, () -> new GZipCompression());

		compressors.put(2, () -> {
			AudioFormat format = soundApi.getMixer().getMicrophoneLine().getFormat();
			return new OpusCompression(format.getSampleRate(), format.isBigEndian());
		});

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
	 * Set the sound API to use to access the player's microphone and speakers. If the previous sound API was already initialized, it
	 * will be disposed and the new sound API will be initialized.
	 * 
	 * @param soundApi The sound API to use.
	 */
	public void setSoundApi(ISoundApi soundApi) {
		if (this.soundApi.getMixer().isInitialized())
			this.soundApi.dispose();

		try {
			soundApi.initialize();
			this.soundApi = soundApi;

			Logger.info("Sound API initialized successfully");
			EventManager.callEvent(new SoundApiInitializedEvent(soundApi));
		} catch (Exception e) {
			Logger.error("An issue occurred while initializing sound API: %s", e.getMessage());
			EventManager.callEvent(new SoundApiInitializationErrorEvent(e));
		}
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
	public boolean registerCompressor(int algorithm, Supplier<ISampleCompressor> compressor) {
		// First 20 values are reserved.
		if (algorithm < 20 || 255 < algorithm)
			return false;

		// An algorithm is already registered for the value.
		if (compressors.containsKey(algorithm))
			return false;

		compressors.put(algorithm, compressor);
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
	public ISampleCompressor getCompressor(int algorithm) {
		Supplier<ISampleCompressor> supplier = compressors.get(algorithm);
		return supplier == null ? getCompressor(0) : supplier.get();
	}
}
