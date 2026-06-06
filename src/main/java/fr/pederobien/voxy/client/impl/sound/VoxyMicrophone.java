package fr.pederobien.voxy.client.impl.sound;

import fr.pederobien.sound.interfaces.IMicrophone;
import fr.pederobien.voxy.client.interfaces.IVoxyMicrophone;

public class VoxyMicrophone implements IVoxyMicrophone {
	private IMicrophone microphone;

	/**
	 * Creates an adapter for a voxy client of a microphone.
	 * 
	 * @param microphone The microphone to use to speak with other players.
	 */
	public VoxyMicrophone(IMicrophone microphone) {
		this.microphone = microphone;
	}

	@Override
	public void open() throws Exception {
		microphone.open();
	}

	@Override
	public void close() throws Exception {
		microphone.close();
	}

	@Override
	public int fetch(byte[] data) throws Exception {
		return microphone.fetch(data);
	}
}
