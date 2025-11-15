package fr.pederobien.voxy.client.impl.internal;

public class Factory {

	/**
	 * Creates the implementation of a voxy client.
	 *
	 * @param name    The player's name.
	 * @param address The server's address.
	 * @param port    The server's port number.
	 */
	public static VoxyClientImpl createClientImpl(String name, String address, int port) {
		return new VoxyClientImpl(name, address, port);
	}
}
