package fr.pederobien.voxy.client.impl.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.pederobien.sound.event.SoundApiInitializationErrorEvent;
import fr.pederobien.sound.event.SoundApiInitializedEvent;
import fr.pederobien.sound.interfaces.ISoundApi;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyMainPlayerSpeakingEvent;
import fr.pederobien.voxy.client.event.VoxyMicrophoneCloseFailureEvent;
import fr.pederobien.voxy.client.event.VoxyMicrophoneOpenFailureEvent;
import fr.pederobien.voxy.client.event.VoxySpeakersCloseFailureEvent;
import fr.pederobien.voxy.client.event.VoxySpeakersOpenFailureEvent;
import fr.pederobien.voxy.client.interfaces.ISampleCompressor;
import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;
import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class SoundApiManager extends ClientElement {
	private final ISoundApi soundApi;
	private SoundApiManagerState current;

	/**
	 * Creates a manager dedicated to interact with the sound API.
	 * 
	 * @param soundApi The interface to access the microphone and speakers.
	 */
	public SoundApiManager(VoxyClientImpl client) {
		super(client);

		soundApi = client.getSoundApi();

		client.getConfig().getVoiceActivityDetector().initialize(soundApi.getMixer().getSampleRate(), 1000, 300);

		if (soundApi.getMixer().isInitialized())
			current = new InitializedState(client.getConfig());
		else {
			try {
				current = new NotInitializedState();
				soundApi.initialize();
				current = new InitializedState(client.getConfig());

				Logger.info("Sound API initialized successfully");
				EventManager.callEvent(new SoundApiInitializedEvent(soundApi));
			} catch (Exception e) {
				Logger.error("An issue occurred while initializing sound API: %s", e.getMessage());
				EventManager.callEvent(new SoundApiInitializationErrorEvent(e));
			}
		}
	}

	/**
	 * Free the resources used by the sound API.
	 */
	public void dispose() {
		current.dispose();
	}

	/**
	 * Enable or disable the microphone.
	 * 
	 * @param isMute True to disable the microphone, false to enable it.
	 */
	public void setMute(boolean isMute) {
		current.setMute(isMute);
	}

	/**
	 * Enable or disable the speakers.
	 * 
	 * @param isDeaf True to disable the speakers, false to enable them.
	 */
	public void setDeaf(boolean isDeaf) {
		current.setDeaf(isDeaf);
	}

	/**
	 * Uncompress the given audio samples before writing it to the speakers.
	 * 
	 * @param name      The player's name associated to this audio sample.
	 * @param sample    The bytes array that contains one or more audio samples.
	 * @param algorithm The algorithm used to compress the audio sample.
	 */
	public void write(String name, byte[] sample, byte algorithm) {
		current.write(name, sample, algorithm);
	}

	/**
	 * Set the left, right and global volumes of an audio stream.
	 * 
	 * @param name   The name of the stream.
	 * @param left   The volume on the left side.
	 * @param right  The volume on the right side.
	 * @param global The global volume on both sides.
	 */
	public void setVolumes(String name, float left, float right, float global) {
		current.setVolumes(name, left, right, global);
	}

	/**
	 * Set to 1.0 the left, right and global volumes of each registered stream.
	 */
	public void resetVolumes() {
		current.resetVolumes();
	}

	/**
	 * Remove the decompressor associated to the given player.
	 * 
	 * @param player The player that left a voxy room.
	 */
	public void remove(String player) {
		current.remove(player);
	}

	private abstract class SoundApiManagerState {

		/**
		 * Free the resources used by the sound API.
		 */
		protected abstract void dispose();

		/**
		 * Enable or disable the microphone.
		 * 
		 * @param isMute True to disable the microphone, false to enable it.
		 */
		protected abstract void setMute(boolean isMute);

		/**
		 * Enable or disable the speakers.
		 * 
		 * @param isDeaf True to disable the speakers, false to enable them.
		 */
		protected abstract void setDeaf(boolean isDeaf);

		/**
		 * Uncompress the given audio sample before writing it to the speakers.
		 * 
		 * @param name      The player's name associated to this audio sample.
		 * @param samples   The bytes array that contains one or more audio samples.
		 * @param algorithm The algorithm used to compress the audio sample.
		 */
		protected abstract void write(String name, byte[] samples, byte algorithm);

		/**
		 * Set the left, right and global volumes of an audio stream.
		 * 
		 * @param name   The name of the stream.
		 * @param left   The volume on the left side.
		 * @param right  The volume on the right side.
		 * @param global The global volume on both sides.
		 */
		protected abstract void setVolumes(String name, float left, float right, float global);

		/**
		 * Set to 1.0 the left, right and global volumes of each registered stream.
		 */
		protected abstract void resetVolumes();

		/**
		 * Remove the decompressor associated to the given player.
		 * 
		 * @param player The player that left a voxy room.
		 */
		protected abstract void remove(String player);
	}

	private class NotInitializedState extends SoundApiManagerState {

		@Override
		protected void dispose() {
			// Do nothing
		}

		@Override
		protected void setMute(boolean isMute) {
			// Do nothing
		}

		@Override
		protected void setDeaf(boolean isDeaf) {
			// Do nothing
		}

		@Override
		protected void write(String name, byte[] sample, byte algorithm) {
			// Do nothing
		}

		@Override
		protected void setVolumes(String name, float left, float right, float global) {
			// Do nothing
		}

		@Override
		protected void resetVolumes() {
			// Do nothing
		}

		@Override
		protected void remove(String player) {
			// Do nothing
		}
	}

	private class InitializedState extends SoundApiManagerState {
		private ISampleCompressor microphoneCompressor;
		private Decompressors decompressors;
		private boolean isMute;
		private boolean isDeaf;
		private Thread fetcher;

		/**
		 * Creates a state corresponding to an initialized sound API.
		 * 
		 * @param config The voxy client configuration to used to know compression algorithm to use.
		 */
		public InitializedState(IVoxyClientConfig config) {
			isMute = true;
			isDeaf = true;

			microphoneCompressor = config.getCompressor(config.getCompressionAlgorithm());
			decompressors = new Decompressors();
		}

		@Override
		protected void dispose() {
			soundApi.dispose();
		}

		@Override
		protected void setMute(boolean isMute) {
			if (this.isMute == isMute)
				return;

			this.isMute = isMute;

			if (isMute)
				closeMicrophone();
			else
				openMicrophone();
		}

		@Override
		protected void setDeaf(boolean isDeaf) {
			if (this.isDeaf == isDeaf)
				return;

			this.isDeaf = isDeaf;

			if (isDeaf)
				closeSpeakers();
			else
				openSpeakers();
		}

		@Override
		protected void write(String name, byte[] samples, byte algorithm) {
			try {
				soundApi.getMixer().write(name, decompress(samples, decompressors.getOrCreateDecompressor(name, algorithm)));
			} catch (Exception e) {
				error("An error occurred while writing audio sample for %s: %s", name, e.getMessage());
			}
		}

		@Override
		protected void setVolumes(String name, float left, float right, float global) {
			soundApi.getMixer().setVolumes(name, left, right, global);
		}

		@Override
		protected void resetVolumes() {
			soundApi.getMixer().resetVolumes();
		}

		@Override
		protected void remove(String player) {
			decompressors.remove(player);
		}

		/**
		 * Fetch data from the microphone and throws a PlayerSpeakingEvent accordingly.
		 */
		private void fetch() {
			try {
				int size = getClient().getSoundApi().getMixer().getMicrophoneLine().getBufferSize() / 5;
				while (!isMute) {
					byte[] sample = new byte[size];
					int read = soundApi.getMicrophone().read(sample);

					// Case the microphone has been closed
					if (isMute || read == -1)
						break;

					// Resizing to optimize band-pass
					if (read != size)
						sample = ByteWrapper.wrap(sample).extract(0, read);

					// Check if sample contains voice
					if (!getClient().getConfig().getVoiceActivityDetector().checkVoiceActivity(sample))
						continue;

					// Sample contains voice -> to be compressed
					IVoxyMainPlayer player = getClient().getPlayer().getExternal();
					byte[] compressed = compress(sample, microphoneCompressor);
					byte algorithm = (byte) microphoneCompressor.getAlgorithm();

					// Notifying player is speaking
					EventManager.callEvent(new VoxyMainPlayerSpeakingEvent(player, compressed, algorithm));
				}
			} catch (Exception e) {
				error("An error occurred while fetching data from the microphone: %s", e.getMessage());
				closeMicrophone();
			}
		}

		/**
		 * Opens the sound API microphone.
		 */
		private void openMicrophone() {
			debug("Opening microphone");
			try {
				getClient().getConfig().getVoiceActivityDetector().reset(false);
				soundApi.getMicrophone().open();

				fetcher = new Thread(this::fetch, "MicrophoneDataSender");
				fetcher.start();
			} catch (Exception e) {
				error("An exception occurred while opening the microphone: %s", e.getMessage());
				EventManager.callEvent(new VoxyMicrophoneOpenFailureEvent(e));
			}
		}

		/**
		 * Closes the sound API microphone.
		 */
		private void closeMicrophone() {
			debug("Closing microphone");
			try {
				soundApi.getMicrophone().close();
			} catch (Exception e) {
				error("An exception occurred while closing the microphone: %s", e.getMessage());
				EventManager.callEvent(new VoxyMicrophoneCloseFailureEvent(e));
			}
		}

		/**
		 * Opens the sound API speakers.
		 */
		private void openSpeakers() {
			debug("Opening speakers");
			try {
				soundApi.getSpeakers().open();
			} catch (Exception e) {
				error("An exception occurred while opening the speakers: %s", e.getMessage());
				EventManager.callEvent(new VoxySpeakersOpenFailureEvent(e));
			}
		}

		/**
		 * Closes the sound API microphone.
		 */
		private void closeSpeakers() {
			debug("Closing speakers");
			try {
				soundApi.getSpeakers().close();
			} catch (Exception e) {
				error("An exception occurred while closing the speakers: %s", e.getMessage());
				EventManager.callEvent(new VoxySpeakersCloseFailureEvent(e));
			}
		}

		/**
		 * Compress the given sample with the given compressor.
		 * 
		 * @param sample     The sample the compress.
		 * @param compressor The compression algorithm to use.
		 * @return The compressed bytes array associated to the given sample to compress.
		 */
		private byte[] compress(byte[] sample, ISampleCompressor compressor) throws Exception {
			ByteWrapper wrapper = ByteWrapper.create();
			int bytesPerFrame = microphoneCompressor.getBytesPerFrame() == -1 ? sample.length : microphoneCompressor.getBytesPerFrame();
			byte[] buffer = new byte[bytesPerFrame];

			// Byte 0 -> 3: Number of packets
			int numberOfPackets = sample.length / bytesPerFrame;
			wrapper.putInt(numberOfPackets);

			for (int i = 0; i < numberOfPackets; i++) {

				// Extracting bytes array to match compressor constraint
				System.arraycopy(sample, i * buffer.length, buffer, 0, buffer.length);

				// Compressing buffer
				byte[] compressed = compressor.compress(buffer);

				// Bytes 4 -> 7: Compressed buffer's length
				wrapper.putInt(compressed.length);

				// Byte 8 -> 8 + compressed buffer's length: data
				wrapper.put(compressed);
			}

			return wrapper.get();
		}

		private byte[] decompress(byte[] samples, ISampleCompressor decompressor) throws Exception {
			ReadableByteWrapper readable = ReadableByteWrapper.wrap(samples);
			ByteWrapper writeable = ByteWrapper.create();

			// Byte 0 -> 3: Number of packets
			int numberOfPackets = readable.nextInt();

			for (int i = 0; i < numberOfPackets; i++) {

				// Byte 4 -> 7: Packet's length
				int length = readable.nextInt();

				// Bytes 8 -> 8 + packet's length: compressed buffer
				byte[] buffer = readable.next(length);

				// Decompressing buffer
				writeable.put(decompressor.decompress(buffer));
			}

			return writeable.get();
		}
	}

	private class Decompressors {
		private final List<StreamDecompressor> table;
		private final Object lock;

		/**
		 * Creates a table of decompressors.
		 * 
		 * @param microphone The compressor to use for the microphone.
		 */
		public Decompressors() {
			table = new ArrayList<StreamDecompressor>();

			lock = new Object();
		}

		/**
		 * Check if a decompressor is already registered for the given name and algorithm. If not, a new decompressor is created.
		 * 
		 * @param name      The name of the stream to decompress.
		 * @param algorithm The algorithm to use for audio samples decompression.
		 * @return The decompressor if it exists, null otherwise.
		 */
		public ISampleCompressor getOrCreateDecompressor(String name, byte algorithm) {
			synchronized (lock) {
				for (StreamDecompressor decompressor : table)
					if (decompressor.getName().equals(name))
						return decompressor.getDecompressor();

				ISampleCompressor decompressor = getClient().getConfig().getCompressor((int) algorithm);
				table.add(new StreamDecompressor(name, decompressor));
				return decompressor;
			}
		}

		/**
		 * Removes the decompressor associated to the given name.
		 * 
		 * @param name The name of the stream.
		 */
		public void remove(String name) {
			synchronized (lock) {
				Iterator<StreamDecompressor> iterator = table.iterator();
				while (iterator.hasNext()) {
					if (iterator.next().getName().equals(name))
						iterator.remove();
				}
			}
		}

		private class StreamDecompressor {
			private final String name;
			private final ISampleCompressor decompressor;

			/**
			 * Creates an association between a stream name and a decompressor.
			 * 
			 * @param name         The name of the stream to decompress.
			 * @param decompressor The decompressor to use for decompression.
			 */
			public StreamDecompressor(String name, ISampleCompressor decompressor) {
				this.name = name;
				this.decompressor = decompressor;
			}

			/**
			 * The name of the stream to decompress.
			 */
			public String getName() {
				return name;
			}

			/**
			 * @return The compressor to use to decompress the stream.
			 */
			public ISampleCompressor getDecompressor() {
				return decompressor;
			}
		}
	}
}
