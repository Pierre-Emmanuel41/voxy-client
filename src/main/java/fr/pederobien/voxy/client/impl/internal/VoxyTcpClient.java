package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.client.EthernetProtocolClientConfig;
import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.protocol.interfaces.IProtocolManager;
import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;
import fr.pederobien.voxy.common.impl.VoxyManagers;

public class VoxyTcpClient {
	private final IProtocolClient client;
	private final ServerNotifier notifier;
	private final ServerRequestHandler handler;

	/**
	 * Creates a TCP client to send / receive request from a voxy server.
	 * 
	 * @param config The configuration to use to create this client.
	 */
	public VoxyTcpClient(VoxyClientImpl voxyClient, IVoxyClientConfig config) {
		String name = config.getName();
		IProtocolManager protocolManager = VoxyManagers.instance().getProtocolManager();
		EthernetProtocolClientConfig configuration = Messenger.createEthernetClientConfig(protocolManager, name, config.getTcpConfig().getEndPoint());
		configuration.setConnectionName(configuration.getName());
		configuration.setConnectionMaxUnstableCounter(config.getTcpConfig().getConnectionMaxUnstableCounter());
		configuration.setConnectionHealTime(config.getTcpConfig().getConnectionHealTime());
		configuration.setConnectionTimeout(config.getTcpConfig().getConnectionTimeout());
		configuration.setAutomaticReconnection(config.getTcpConfig().isAutomaticReconnection());
		configuration.setReconnectionDelay(config.getTcpConfig().getReconnectionDelay());
		configuration.setLayerInitializer(config.getTcpConfig().getLayerInitializer());
		configuration.setClientMaxUnstableCounter(config.getTcpConfig().getClientMaxUnstableCounter());
		configuration.setClientHealTime(config.getTcpConfig().getClientHealTime());
		client = Messenger.createTcpClient(configuration);

		notifier = new ServerNotifier(client, configuration, voxyClient.getPlayer());
		handler = new ServerRequestHandler(client, configuration, voxyClient);
		handler.setEnabled(true);
	}

	@Override
	public String toString() {
		return client.toString();
	}

	/**
	 * @return The object that sends request to the server.
	 */
	public ServerNotifier getNotifier() {
		return notifier;
	}

	/**
	 * @return The object that receives request to the server.
	 */
	public ServerRequestHandler getHandler() {
		return handler;
	}

	/**
	 * The implementation shall try establishing the connection only when this method is called. The class is expected to retry
	 * establishing the connection as long as Disconnected() is not called. Timeout may be reported in event LogEvent.
	 */
	public void connect() {
		client.connect();
	}

	/**
	 * Close the connection to the remote.
	 */
	public void disconnect() {
		client.disconnect();
	}

	/**
	 * Dispose this connection. After this, it is impossible to send data to the remote using this connection.
	 */
	public void dispose() {
		client.dispose();
	}

	/**
	 * @return True if the connection is disposed and cannot be used anymore.
	 */
	public boolean isDisposed() {
		return client.isDisposed();
	}

	/**
	 * @return The connection to send /receive requests from the server.
	 */
	public IProtocolConnection getConnection() {
		return client.getConnection();
	}
}
