package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.messenger.event.ProtocolConnectionLostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.client.impl.VoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyClientConfig;

public class VoxyClientImpl implements IEventListener {
	private final IVoxyClientConfig config;
	private final VoxyTcpClient tcpClient;
	private final VoxyMainPlayerImpl player;
	private final RoomListImpl rooms;

	private final IVoxyClient external;

	/**
	 * Creates the implementation of a voxy client.
	 * 
	 * @param config The configuration that gather parameters to create a voxy client.
	 */
	protected VoxyClientImpl(IVoxyClientConfig config) {
		this.config = config;

		tcpClient = new VoxyTcpClient(this, config);
		player = new VoxyMainPlayerImpl(this, config.getName());
		rooms = new RoomListImpl(this);

		external = new VoxyClient(this);
		EventManager.registerListener(this);
	}

	@Override
	public String toString() {
		return tcpClient.toString();
	}

	/**
	 * Opens the connection with the server. When the connection is established, a VoxyClientConnected event is thrown.
	 */
	public void connect() {
		tcpClient.connect();
	}

	/**
	 * Close the connection with the server.
	 */
	public void disconnect() {
		rooms.clear();
		player.getVocalClient().disconnect();
		tcpClient.disconnect();
	}

	/**
	 * Dispose this client. It cannot be reused to communicate with the remote.
	 */
	public void dispose() {
		player.getVocalClient().dispose();
		tcpClient.dispose();
	}

	/**
	 * @return True if the client is disposed, false otherwise..
	 */
	public boolean isDisposed() {
		return tcpClient.isDisposed();
	}

	/**
	 * @return The implementation of a rooms list.
	 */
	public RoomListImpl getRooms() {
		return rooms;
	}

	/**
	 * @return The implementation of a voxy main player.
	 */
	public VoxyMainPlayerImpl getPlayer() {
		return player;
	}

	/**
	 * @return The configuration of the voxy client.
	 */
	public IVoxyClientConfig getConfig() {
		return config;
	}

	/**
	 * @return The voxy client to use externally.
	 */
	public IVoxyClient getExternal() {
		return external;
	}

	/**
	 * @return The object that sends request to the server.
	 */
	public ServerNotifier getNotifier() {
		return tcpClient.getNotifier();
	}

	@EventHandler
	private void onConnectionLost(ProtocolConnectionLostEvent event) {
		if (event.getConnection() != tcpClient.getConnection())
			return;

		rooms.clear();
		player.getVocalClient().disconnect();
	}
}
