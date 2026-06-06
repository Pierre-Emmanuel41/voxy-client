package fr.pederobien.voxy.client.impl.sound;

import fr.pederobien.sound.impl.SoundApi;
import fr.pederobien.sound.interfaces.IMixer;
import fr.pederobien.sound.interfaces.ISoundApi;
import fr.pederobien.voxy.client.interfaces.IVoxyMicrophone;
import fr.pederobien.voxy.client.interfaces.IVoxySoundApi;
import fr.pederobien.voxy.client.interfaces.IVoxySpeakers;

public class VoxySoundApi implements IVoxySoundApi {
	private ISoundApi soundApi;
	private IVoxyMicrophone microphone;
	private IVoxySpeakers speakers;

	/**
	 * Creates a simple sound API adapter for a voxy client.
	 */
	public VoxySoundApi() {
		soundApi = new SoundApi();
	}

	/**
	 * Creates a sound API adapter for a voxy client and specifies how player's stream shall be handled.
	 * 
	 * @param mixer The mixer to use to process raw microphone output and to manager player's audio stream.
	 */
	public VoxySoundApi(IMixer mixer) {
		soundApi = new SoundApi(mixer);
	}

	/**
	 * Creates a sound API adapter for a voxy client.
	 * 
	 * @param soundApi The API to use to access the microphone and the speakers.
	 */
	public VoxySoundApi(ISoundApi soundApi) {
		this.soundApi = soundApi;
	}

	@Override
	public void initialize() throws Exception {
		soundApi.initialize();
		microphone = new VoxyMicrophone(soundApi.getMicrophone());
		speakers = new VoxySpeakers(soundApi.getSpeakers());
	}

	@Override
	public void dispose() {
		soundApi.dispose();
	}

	@Override
	public IVoxyMicrophone getMicrophone() {
		return microphone;
	}

	@Override
	public IVoxySpeakers getSpeakers() {
		return speakers;
	}
}
