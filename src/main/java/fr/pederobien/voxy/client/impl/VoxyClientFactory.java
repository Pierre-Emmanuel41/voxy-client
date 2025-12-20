package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.impl.internal.Factory;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyMicrophone;
import fr.pederobien.voxy.client.interfaces.IVoxySpeakers;

public class VoxyClientFactory {

	/**
	 * Creates a client to communicate with a voxy server.
	 *
	 * @param name       The player's name.
	 * @param address    The server's address.
	 * @param port       The server's port number.
	 * @param microphone The microphone to use to send audio samples to the server.
	 * @param speakers   The speakers to use to player audio samples received from the server.
	 */
	public static final IVoxyClient create(String name, String address, int port, IVoxyMicrophone microphone, IVoxySpeakers speakers) {
		return Factory.createClientImpl(name, address, port, microphone, speakers).getExternal();
	}
}
