package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.voxy.client.interfaces.IVoxySoundApi;

public class Factory {

	/**
	 * Creates the implementation of a voxy client.
	 *
	 * @param name     The player's name.
	 * @param address  The server's address.
	 * @param port     The server's port number.
	 * @param soundApi The API to use to access the microphone and the speakers.
	 */
	public static VoxyClientImpl createClientImpl(String name, String address, int port, IVoxySoundApi soundApi) {
		return new VoxyClientImpl(name, address, port, soundApi);
	}
}
