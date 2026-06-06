package fr.pederobien.voxy.client.impl.config;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.voxy.client.impl.sound.VoxySoundApi;
import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;
import fr.pederobien.voxy.client.interfaces.IVoxySoundApi;

public class VoxyClientConfig implements IVoxyClientConfig {
	private final String name;
	private final VoxyTcpConfig tcpConfig;
	private final VoxyUdpConfig udpConfig;
	private IVoxySoundApi soundApi;

	/**
	 * Creates a configuration for a voxy client.
	 * 
	 * @param name     The name of the player.
	 * @param endPoint The server's end-point (IP address + port number);
	 */
	public VoxyClientConfig(String name, IEthernetEndPoint endPoint) {
		this.name = name;

		tcpConfig = new VoxyTcpConfig(endPoint);
		udpConfig = new VoxyUdpConfig();
		soundApi = new VoxySoundApi();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public VoxyTcpConfig getTcpConfig() {
		return tcpConfig;
	}

	@Override
	public VoxyUdpConfig getUdpConfig() {
		return udpConfig;
	}

	@Override
	public IVoxySoundApi getSoundApi() {
		return soundApi;
	}

	/**
	 * Set the sound API to use to access the player's microphone and speakers.
	 * 
	 * @param soundApi The sound API to use.
	 */
	public void setSoundApi(IVoxySoundApi soundApi) {
		this.soundApi = soundApi;
	}
}
