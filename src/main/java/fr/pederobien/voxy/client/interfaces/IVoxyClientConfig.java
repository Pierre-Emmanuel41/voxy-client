package fr.pederobien.voxy.client.interfaces;

public interface IVoxyClientConfig {

	/**
	 * @return The player's name.
	 */
	String getName();

	/**
	 * @return The configuration to use for the TCP client.
	 */
	IVoxyTcpConfig getTcpConfig();

	/**
	 * @return The configuration to use for the UDP client.
	 */
	IVoxyUdpConfig getUdpConfig();

	/**
	 * @return The API to use to access the microphone and the speakers.
	 */
	IVoxySoundApi getSoundApi();
}
