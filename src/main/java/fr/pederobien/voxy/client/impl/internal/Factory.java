package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.voxy.client.interfaces.IVoxyMicrophone;
import fr.pederobien.voxy.client.interfaces.IVoxySpeakers;

public class Factory {

	/**
	 * Creates the implementation of a voxy client.
	 *
	 * @param name       The player's name.
	 * @param address    The server's address.
	 * @param port       The server's port number.
	 * @param microphone The microphone to use to send audio samples to the server.
	 * @param speakers   The speakers to use to player audio samples received from the server.
	 */
	public static VoxyClientImpl createClientImpl(String name, String address, int port, IVoxyMicrophone microphone, IVoxySpeakers speakers) {
		return new VoxyClientImpl(name, address, port, microphone, speakers);
	}
}
