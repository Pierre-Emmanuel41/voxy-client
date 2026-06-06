package fr.pederobien.voxy.client.impl.config;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.voxy.client.interfaces.IVoxyTcpConfig;

public class VoxyTcpConfig extends VoxyConfig implements IVoxyTcpConfig {
	private final IEthernetEndPoint endPoint;
	private boolean automaticReconnection;

	/**
	 * Creates a configuration for the TCP client of the voxy client. The TCP client is used to configure a voxy server.
	 * 
	 * @param endPoint The server's end point (IP address + port number)
	 */
	public VoxyTcpConfig(IEthernetEndPoint endPoint) {
		this.endPoint = endPoint;

		automaticReconnection = true;
	}

	@Override
	public boolean isAutomaticReconnection() {
		return automaticReconnection;
	}

	/**
	 * Set if the client should automatically reconnect if a network error occurs. The default value is true.
	 *
	 * @param automaticReconnection True to automatically reconnect, false otherwise.
	 */
	public void setAutomaticReconnection(boolean automaticReconnection) {
		this.automaticReconnection = automaticReconnection;
	}

	@Override
	public IEthernetEndPoint getEndPoint() {
		return endPoint;
	}
}
