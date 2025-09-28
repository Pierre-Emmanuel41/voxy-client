package fr.pederobien.voxy.client.impl;

import java.util.function.Consumer;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.client.ProtocolClientConfig;
import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;

public class VoxyClient implements IVoxyClient {
	private final String playerName;
	private final ProtocolClientConfig<IEthernetEndPoint> config;
	private final IProtocolClient client;
	private Consumer<Boolean> callback;

	/**
	 * Creates a client to communicate with a voxy server.
	 *
	 * @param playerName The player's name.
	 * @param address    The server's address.
	 * @param port       The server's port number.
	 */
	protected VoxyClient(String playerName, String address, int port) {
		this.playerName = playerName;

		IEthernetEndPoint endPoint = new EthernetEndPoint(address, port);
		config = Messenger.createClientConfig(VoxyProtocolManager.instance(), "Voxy_Client", endPoint);

		// Layer to communicate: RSA
		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		// Registering event handler
		config.addRequestHandler(VoxyIdentifiers.ADD_ROOM, this::onRoomAdded);
		config.addRequestHandler(VoxyIdentifiers.REMOVE_ROOM, this::onRoomRemoved);
		config.addRequestHandler(VoxyIdentifiers.RENAME_ROOM, this::onRoomRenamed);
		config.addRequestHandler(VoxyIdentifiers.PLAYER_PROPERTIES, this::onPlayerProperties);

		client = Messenger.createTcpClient(config);
	}

	@Override
	public void connect(Consumer<Boolean> callback) {
		this.callback = callback;
		client.connect();
	}

	@Override
	public void disconnect() {
		client.disconnect();
	}

	@Override
	public void dispose() {
		client.dispose();
	}

	@Override
	public boolean isDisposed() {
		return client.isDisposed();
	}

	private void onRoomAdded(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof AddRoomRequest request))
			return;

		Logger.info("A room %s shall be added, communication on port %s", request.getName(), request.getPort());
	}

	private void onRoomRemoved(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RemoveRoomRequest request))
			return;

		Logger.info("The room %s shall be removed", request.getName());
	}

	private void onRoomRenamed(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RenameRoomRequest request))
			return;

		Logger.info("The room %s shall be renamed as %s", request.getOldName(), request.getNewName());
	}

	private void onPlayerProperties(IProtocolConnection connection, int messageID, Object ignored) {
		PlayerPropertiesRequest request = new PlayerPropertiesRequest(playerName, false, false);

		IRequestMessage response = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, request);
		// A timeout means the server accepts the connection
		response.setCallback(args -> callback.accept(config.parse(args.response()).getError() == VoxyErrors.NO_ERROR));
		answer(messageID, response);
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param error      The request error.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	private IRequestMessage getRequest(IIdentifier identifier, IError error, Object payload) {
		return config.getRequest(identifier, error, payload);
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	private IRequestMessage getRequest(IIdentifier identifier, Object payload) {
		return getRequest(identifier, VoxyErrors.NO_ERROR, payload);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param request The request to send to the remote.
	 */
	private void send(IRequestMessage request) {
		client.getConnection().send(request);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param messageID The identifier of the message received from the remote.
	 * @param request   The request to send to the remote.
	 */
	private void answer(int messageID, IRequestMessage request) {
		client.getConnection().answer(messageID, request);
	}

	/**
	 * Parse the given bytes array to get the payload.
	 *
	 * @param data The raw bytes array to parse.
	 * @return The payload parsed, or null if a ClassCastException occurred.
	 */
	@SuppressWarnings("unchecked")
	private <T> T parse(byte[] data) {
		try {
			return (T) config.parse(data).getPayload();
		} catch (ClassCastException e) {
			return null;
		}
	}
}
