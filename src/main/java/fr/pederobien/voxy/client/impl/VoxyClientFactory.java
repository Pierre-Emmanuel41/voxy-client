package fr.pederobien.voxy.client.impl;

import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.voxy.client.impl.internal.Factory;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxySoundApi;

public class VoxyClientFactory {

	/**
	 * Creates a client to communicate with a voxy server. If the sound API could not be initialized successfully, a
	 * SoundApiInitializationErrorEvent is thrown otherwise a SoundApiInitializedEvent is thrown.
	 *
	 * @param name        The player's name.
	 * @param address     The server's address.
	 * @param port        The server's port number.
	 * @param certificate The certificate to use to sign / authenticate requests.
	 * @param soundApi    The API to use to access the microphone and the speakers.
	 */
	public static final IVoxyClient create(String name, String address, int port, ICertificate certificate, IVoxySoundApi soundApi) {
		return Factory.createClientImpl(name, address, port, certificate, soundApi).getExternal();
	}

	/**
	 * Creates a client to communicate with a voxy server. If the sound API could not be initialized successfully, a
	 * SoundApiInitializationErrorEvent is thrown otherwise a SoundApiInitializedEvent is thrown.
	 *
	 * @param name     The player's name.
	 * @param address  The server's address.
	 * @param port     The server's port number.
	 * @param soundApi The API to use to access the microphone and the speakers.
	 */
	public static final IVoxyClient create(String name, String address, int port, IVoxySoundApi soundApi) {
		return create(name, address, port, new SimpleCertificate(), soundApi);
	}
}
