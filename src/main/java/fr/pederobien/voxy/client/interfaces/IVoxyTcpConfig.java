package fr.pederobien.voxy.client.interfaces;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;

public interface IVoxyTcpConfig extends IVoxyConfig {

	/**
	 * @return True if the client should try to reconnect automatically with the server if an error occurred. The default value is
	 *         true.
	 */
	boolean isAutomaticReconnection();

	/**
	 * @return The object that gather remote information.
	 */
	IEthernetEndPoint getEndPoint();
}
