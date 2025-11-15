package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.client.ProtocolClientConfig;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.voxy.client.impl.VoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;

public class VoxyClientImpl {
	private final ProtocolClientConfig<IEthernetEndPoint> config;
	private final IProtocolClient client;
	private final VoxyMainPlayerImpl player;
	private final RoomListImpl rooms;
	private final ServerNotifier notifier;
	private final ServerRequestHandler handler;

	private final IVoxyClient external;

	/**
	 * Creates the implementation of a voxy client.
	 * 
	 * @param name    The player's name.
	 * @param address The server's address.
	 * @param port    The server's port number.
	 */
	protected VoxyClientImpl(String name, String address, int port) {
		IEthernetEndPoint endPoint = new EthernetEndPoint(address, port);
		config = Messenger.createClientConfig(VoxyProtocolManager.instance(), name, endPoint);

		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		client = Messenger.createTcpClient(config);

		player = new VoxyMainPlayerImpl(this, name);
		rooms = new RoomListImpl(this);
		notifier = new ServerNotifier(this);
		handler = new ServerRequestHandler(this);
		handler.setEnabled(true);

		external = new VoxyClient(this);
	}

	@Override
	public String toString() {
		return client.toString();
	}

	/**
	 * Opens the connection with the server. When the connection is established, a VoxyClientConnected event is thrown.
	 */
	public void connect() {
		client.connect();
	}

	/**
	 * Close the connection with the server.
	 */
	public void disconnect() {
		player.getVocalClient().disconnect();
		client.disconnect();
	}

	/**
	 * Dispose this client. It cannot be reused to communicate with the remote.
	 */
	public void dispose() {
		client.dispose();
	}

	/**
	 * @return True if the client is disposed, false otherwise..
	 */
	public boolean isDisposed() {
		return client.isDisposed();
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
	 * @return The server's address.
	 */
	public String getAddress() {
		return config.getEndPoint().getAddress();
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
		return notifier;
	}

	/**
	 * @return The client used to communicate with the server.
	 */
	protected IProtocolClient getClient() {
		return client;
	}

	/**
	 * @return The configuration used to create/parse requests.
	 */
	protected ProtocolClientConfig<IEthernetEndPoint> getConfig() {
		return config;
	}
}
