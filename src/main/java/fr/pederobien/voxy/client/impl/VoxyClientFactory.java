package fr.pederobien.voxy.client.impl;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.sound.interfaces.ISoundApi;
import fr.pederobien.voxy.client.impl.config.VoxyClientConfig;
import fr.pederobien.voxy.client.impl.internal.Factory;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;

public class VoxyClientFactory {

	/**
	 * Creates a configuration to create a voxy client.
	 * 
	 * @param name    The player's name.
	 * @param address The server's IP address.
	 * @param port    The server's port number.
	 * @return The configuration used to create a voxy client.
	 */
	public static final VoxyClientConfig createConfig(String name, String address, int port) {
		return new VoxyClientConfig(name, new EthernetEndPoint(address, port));
	}

	/**
	 * Creates a voxy client based on the given configuration.
	 * 
	 * @param config The configuration that gather parameters to create a voxy client.
	 * @return The created voxy client.
	 */
	public static final IVoxyClient createClient(IVoxyClientConfig config) {
		return Factory.createClientImpl(config).getExternal();
	}

	/**
	 * Creates a Voxy client based on default configuration parameters.
	 * 
	 * @param name    The player's name
	 * @param address The server's IP address.
	 * @param port    The server's port number.
	 * @return The created voxy client.
	 */
	public static final IVoxyClient createDefault(String name, String address, int port) {
		return createClient(createConfig(name, address, port));
	}

	/**
	 * Creates a Voxy client based on default configuration parameters and sound API.
	 * 
	 * @param name     The player's name
	 * @param address  The server's IP address.
	 * @param port     The server's port number.
	 * @param soundApi The API to use to access the OS microphone and speakers.
	 * @return The created voxy client.
	 */
	public static final IVoxyClient createDefault(String name, String address, int port, ISoundApi soundApi) {
		VoxyClientConfig config = createConfig(name, address, port);
		config.setSoundApi(soundApi);

		return createClient(config);
	}
}
