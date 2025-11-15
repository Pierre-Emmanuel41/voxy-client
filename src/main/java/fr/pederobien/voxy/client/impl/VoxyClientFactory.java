package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.impl.internal.Factory;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;

public class VoxyClientFactory {

	/**
	 * Creates a client to communicate with a voxy server.
	 *
	 * @param name    The player's name.
	 * @param address The server's address.
	 * @param port    The server's port number.
	 */
	public static final IVoxyClient create(String name, String address, int port) {
		return Factory.createClientImpl(name, address, port).getExternal();
	}
}
