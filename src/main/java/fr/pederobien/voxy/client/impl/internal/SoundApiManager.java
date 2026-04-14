package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.client.event.SoundApiInitializationErrorEvent;
import fr.pederobien.voxy.client.event.SoundApiInitializedEvent;
import fr.pederobien.voxy.client.event.VoxyMainPlayerSpeakingEvent;
import fr.pederobien.voxy.client.event.VoxyMicrophoneCloseFailureEvent;
import fr.pederobien.voxy.client.event.VoxyMicrophoneOpenFailureEvent;
import fr.pederobien.voxy.client.event.VoxySpeakersCloseFailureEvent;
import fr.pederobien.voxy.client.event.VoxySpeakersOpenFailureEvent;
import fr.pederobien.voxy.client.interfaces.IVoxySoundApi;

public class SoundApiManager extends ClientElement {
	private static final int SAMPLE_SIZE = 8820;
	private final IVoxySoundApi soundApi;
	private SoundApiManagerState notInitialized;
	private SoundApiManagerState initialized;
	private SoundApiManagerState current;

	/**
	 * Creates a manager dedicated to interact with the sound API.
	 * 
	 * @param soundApi The interface to access the microphone and speakers.
	 */
	public SoundApiManager(VoxyClientImpl client) {
		super(client);

		soundApi = client.getSoundApi();
		notInitialized = new NotInitializedState();
		initialized = new InitializedState();
		current = notInitialized;

		try {
			debug("Initializing sound API");
			soundApi.initialize();
			debug("Sound API initialized successfully");
			current = initialized;
			EventManager.callEvent(new SoundApiInitializedEvent());
		} catch (Exception e) {
			error("An issue occurred while initializing sound API: %s", e.getMessage());
			EventManager.callEvent(new SoundApiInitializationErrorEvent(e));
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
	 * Uncompress the given audio sample before writing it to the speakers.
	 * 
	 * @param name      The player's name associated to this audio sample.
	 * @param sample    The bytes array that contains the audio sample.
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
		 * @param sample    The bytes array that contains the audio sample.
		 * @param algorithm The algorithm used to compress the audio sample.
		 */
		protected abstract void write(String name, byte[] sample, byte algorithm);

		/**
		 * Set the left, right and global volumes of an audio stream.
		 * 
		 * @param name   The name of the stream.
		 * @param left   The volume on the left side.
		 * @param right  The volume on the right side.
		 * @param global The global volume on both sides.
		 */
		protected abstract void setVolumes(String name, float left, float right, float global);
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
	}

	private class InitializedState extends SoundApiManagerState {
		private boolean isMute;
		private boolean isDeaf;
		private Thread fetcher;

		public InitializedState() {
			isMute = true;
			isDeaf = true;
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
		protected void write(String name, byte[] sample, byte algorithm) {
			byte[] uncompressed = BandPassOptimizer.uncompress(sample, algorithm);
			if (uncompressed == null)
				return;

			soundApi.getSpeakers().write(name, uncompressed);
		}

		@Override
		protected void setVolumes(String name, float left, float right, float global) {
			soundApi.getSpeakers().setVolumes(name, left, right, global);
		}

		/**
		 * Fetch data from the microphone and throws a PlayerSpeakingEvent accordingly.
		 */
		private void fetch() {
			byte algorithm = BandPassOptimizer.GZIP;
			try {
				while (!isMute) {
					byte[] sample = new byte[SAMPLE_SIZE];
					int written = soundApi.getMicrophone().fetch(sample);

					// Case the microphone has been closed
					if (isMute || written == -1)
						break;

					// Resizing to optimize band-pass
					if (written != SAMPLE_SIZE)
						sample = ByteWrapper.wrap(sample).extract(0, written);

					// Compressing the raw bytes array
					byte[] compressed = BandPassOptimizer.compress(sample, algorithm);
					if (compressed == null)
						continue;

					// Notifying player is speaking
					EventManager.callEvent(new VoxyMainPlayerSpeakingEvent(getClient().getPlayer().getExternal(), compressed, algorithm));
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
	}
}
