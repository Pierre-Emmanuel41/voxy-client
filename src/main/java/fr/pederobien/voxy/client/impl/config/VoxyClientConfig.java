package fr.pederobien.voxy.client.impl.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.sound.sampled.AudioFormat;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.sound.impl.SoundApi;
import fr.pederobien.sound.impl.effects.EchoEffect;
import fr.pederobien.sound.impl.effects.NoEffect;
import fr.pederobien.sound.interfaces.IEffect;
import fr.pederobien.sound.interfaces.IEffectParametersHolder;
import fr.pederobien.sound.interfaces.IFilter;
import fr.pederobien.sound.interfaces.ISoundApi;
import fr.pederobien.voxy.client.impl.compressors.GZipCompression;
import fr.pederobien.voxy.client.impl.compressors.NoCompression;
import fr.pederobien.voxy.client.impl.compressors.OpusCompression;
import fr.pederobien.voxy.client.impl.internal.VoiceActivityDetector;
import fr.pederobien.voxy.client.interfaces.IEffectBuilder;
import fr.pederobien.voxy.client.interfaces.ISampleCompressor;
import fr.pederobien.voxy.client.interfaces.IVoiceActivityDetector;
import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;

public class VoxyClientConfig implements IVoxyClientConfig {
	private final String name;
	private final VoxyTcpConfig tcpConfig;
	private final VoxyUdpConfig udpConfig;
	private final Map<Integer, Supplier<ISampleCompressor>> compressors;
	private final Map<String, IEffectBuilder> builders;
	private ISoundApi soundApi;
	private IFilter filter;
	private IVoiceActivityDetector vad;
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

		builders = new HashMap<String, IEffectBuilder>();
		builders.put(NoEffect.NAME, new NoEffectBuilder());
		builders.put(EchoEffect.NAME, new EchoEffectBuilder());
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
		this.soundApi = soundApi;
	}

	@Override
	public IFilter getMicrophoneFilter() {
		return filter;
	}

	/**
	 * Set the filter to apply on the microphone's audio stream. The given filter will be set for the microphone once the sound API is
	 * initialized successfully.
	 * 
	 * @param filter The filter to apply on the microphone's audio stream.
	 */
	public void setMicrophoneFilter(IFilter filter) {
		this.filter = filter;
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

	/**
	 * Registers an algorithm to compress / decompress raw microphone's audio sample.
	 * 
	 * @param algorithm  The algorithm number associated to the supplier.
	 * @param compressor An algorithm to compress / decompress audio samples.
	 * @return True if the algorithm has been registered successfully.
	 */
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

	/**
	 * Register an effect builder. The builder is used to create effects.
	 * 
	 * @param name    The name of the effect.
	 * @param builder The associated builder.
	 * @return True if the supplier has been registered successfully, false if a builder is already registered for the given name.
	 */
	public boolean registerEffectBuilder(String name, IEffectBuilder builder) {
		IEffectBuilder registered = builders.get(name);
		if (registered != null)
			return false;

		builders.put(name, builder);
		return true;
	}

	@Override
	public IEffect createEffect(String name, Map<String, Object> values) {
		IEffectBuilder builder = builders.get(name);
		if (builder == null)
			return null;

		IEffectParametersHolder holder = builder.createHolder(values);
		if (holder == null)
			return null;

		return builder.createEffect(soundApi.getMixer().getSampleRate(), holder);
	}

	@Override
	public IEffectParametersHolder createHolder(String name, Map<String, Object> values) {
		IEffectBuilder builder = builders.get(name);
		if (builder == null)
			return null;

		IEffectParametersHolder holder = builder.createHolder(values);
		return holder;
	}
}
