package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;

public class Factory {

	/**
	 * Creates the implementation of a voxy client.
	 *
	 * @param config The configuration that gather parameters to create a voxy client.
	 */
	public static VoxyClientImpl createClientImpl(IVoxyClientConfig config) {
		return new VoxyClientImpl(config);
	}
}
