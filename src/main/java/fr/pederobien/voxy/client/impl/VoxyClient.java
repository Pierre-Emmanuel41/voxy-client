package fr.pederobien.voxy.client.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.client.ProtocolClientConfig;
import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.protocol.interfaces.IRequest;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyClientConnected;
import fr.pederobien.voxy.client.event.VoxyRoomAddedEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRemovedEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRenameRequestEvent;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.PlayerInfo;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.RoomInfo;

public class VoxyClient implements IVoxyClient, IEventListener {
	private final String playerName;
	private final ProtocolClientConfig<IEthernetEndPoint> config;
	private final IProtocolClient client;
	private final Map<String, VoxyRoom> rooms;
	private final Object lock;

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
		config = Messenger.createClientConfig(VoxyProtocolManager.instance(), playerName, endPoint);

		// Layer to communicate: RSA
		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		// Registering event handler
		config.addRequestHandler(VoxyIdentifiers.ADD_ROOM, this::onRoomAdded);
		config.addRequestHandler(VoxyIdentifiers.REMOVE_ROOM, this::onRoomRemoved);
		config.addRequestHandler(VoxyIdentifiers.RENAME_ROOM, this::onRoomRenamed);
		config.addRequestHandler(VoxyIdentifiers.PLAYER_PROPERTIES, this::onPlayerProperties);

		client = Messenger.createTcpClient(config);

		rooms = new HashMap<String, VoxyRoom>();
		lock = new Object();

		EventManager.registerListener(this);
	}

	@Override
	public void connect() {
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

	@Override
	public Map<String, IVoxyRoom> getRooms() {
		return Collections.unmodifiableMap(rooms);
	}

	@Override
	public void add(String name, Consumer<Boolean> callback) {
		debug("Sending a request to the server to add room %s", name);
		IRequestMessage request = getRequest(VoxyIdentifiers.ADD_ROOM, new AddRoomRequest(name, 0));
		request.setCallback(args -> {
			if (!args.isTimeout() && !args.isConnectionLost()) {
				callback.accept(config.parse(args.response()).getError() == VoxyErrors.NO_ERROR);
			} else
				// No response from the server
				callback.accept(false);
		});

		send(request);
	}

	@Override
	public void remove(String name, Consumer<Boolean> callback) {
		debug("Sending a request to the server to remove room %s", name);
		IRequestMessage request = getRequest(VoxyIdentifiers.REMOVE_ROOM, new RemoveRoomRequest(name));
		request.setCallback(args -> {
			if (!args.isTimeout() && !args.isConnectionLost()) {
				callback.accept(config.parse(args.response()).getError() == VoxyErrors.NO_ERROR);
			} else
				// No response from the server
				callback.accept(false);
		});

		send(request);
	}

	@Override
	public String toString() {
		return client.toString();
	}

	@EventHandler
	private void onRoomRenameEvent(VoxyRoomRenameRequestEvent event) {
		if (event.getRoom().getClient() != this)
			return;

		debug("Sending a request to the server to rename room %s as %s", event.getRoom().getName(), event.getNewName());
		IRequestMessage request = getRequest(VoxyIdentifiers.RENAME_ROOM, new RenameRoomRequest(event.getRoom().getName(), event.getNewName()));
		request.setCallback(args -> {
			if (!args.isTimeout() && !args.isConnectionLost()) {
				event.getCallback().accept(config.parse(args.response()).getError() == VoxyErrors.NO_ERROR);
			} else
				// No response from the server
				event.getCallback().accept(false);
		});

		send(request);
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been added.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the new room.
	 */
	private void onRoomAdded(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof AddRoomRequest request))
			return;

		debug("Receiving request to add room %s", request.getName());
		VoxyRoom room = new VoxyRoom(this, request.getName(), request.getPort(), new HashMap<String, IVoxyPlayer>());

		synchronized (lock) {
			rooms.put(request.getName(), room);
		}

		EventManager.callEvent(new VoxyRoomAddedEvent(room));
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been removed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the removed room.
	 */
	private void onRoomRemoved(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RemoveRoomRequest request))
			return;

		debug("Receiving request to remove room %s", request.getName());

		IVoxyRoom room;

		synchronized (lock) {
			room = rooms.remove(request.getName());
		}

		EventManager.callEvent(new VoxyRoomRemovedEvent(room));
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been renamed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the renamed room.
	 */
	private void onRoomRenamed(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RenameRoomRequest request))
			return;

		debug("Receiving request to rename room %s as %s", request.getOldName(), request.getNewName());
		synchronized (lock) {
			VoxyRoom room = rooms.remove(request.getOldName());
			rooms.put(request.getNewName(), room);
			room.setName(request.getNewName());
		}
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been added.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the new room.
	 */
	private void onPlayerProperties(IProtocolConnection connection, int messageID, Object ignored) {
		debug("Server requires player's properties");
		PlayerPropertiesRequest payload = new PlayerPropertiesRequest(playerName, false, false);

		debug("Sending following payload: %s", payload);
		IRequestMessage response = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, payload);
		response.setCallback(args -> handlePlayerPropertiesResponse(args));
		answer(messageID, response);
	}

	/**
	 * Method called when the server accepts or not player's properties.
	 *
	 * @param argument The argument that contains server's response.
	 */
	private void handlePlayerPropertiesResponse(CallbackArgs argument) {
		if (!(argument.isTimeout() || argument.isConnectionLost())) {
			IRequest response = config.parse(argument.response());

			if (response == null) {
				Logger.error("Technical error happened: Could not parse server's response for player's properties");
				EventManager.callEvent(new VoxyClientConnected(this, false));
				return;
			}

			if (response.getIdentifier() != VoxyIdentifiers.ACKOWLEDGEMENT) {
				debug("The server did not acknowledge back player's properties");
				EventManager.callEvent(new VoxyClientConnected(this, false));
				return;
			}

			if (response.getError() != VoxyErrors.NO_ERROR) {
				debug("The server did not accept player properties: %s", response.getError().getMessage());
				EventManager.callEvent(new VoxyClientConnected(this, false));
				return;
			}

			// Sending request to get server's properties
			debug("Requiring server's properties");
			IRequestMessage request = getRequest(VoxyIdentifiers.SERVER_PROPERTIES, new ServerPropertiesRequest());
			request.setCallback(args -> handleServerProperties(args));
			send(request);

		} else {
			debug("A timeout or a connection lost happened after the client sent player's properties");
			EventManager.callEvent(new VoxyClientConnected(this, false));
		}
	}

	/**
	 * Method called when the server sends it properties.
	 *
	 * @param argument The argument that contains server's response.
	 */
	private void handleServerProperties(CallbackArgs argument) {
		if (!(argument.isTimeout() || argument.isConnectionLost())) {
			IRequest response = config.parse(argument.response());

			if (response == null) {
				Logger.error("Technical error happened: Could not parse server's response for server's properties");
				EventManager.callEvent(new VoxyClientConnected(this, false));
				return;
			}

			if (response.getError() != VoxyErrors.NO_ERROR) {
				debug("The server did not accept server's properties: %s", response.getError().getMessage());
				EventManager.callEvent(new VoxyClientConnected(this, false));
				return;
			}

			ServerPropertiesRequest serverProperties = (ServerPropertiesRequest) response.getPayload();
			for (RoomInfo roomInfo : serverProperties.getRooms()) {

				Map<String, IVoxyPlayer> players = new HashMap<String, IVoxyPlayer>();
				for (PlayerInfo playerInfo : roomInfo.players())
					players.put(playerInfo.name(), new VoxyPlayer(this, playerInfo.name(), playerInfo.isMute(), playerInfo.isDeaf()));

				rooms.put(roomInfo.name(), new VoxyRoom(this, roomInfo.name(), roomInfo.port(), players));
			}

			info("%s successfully joined the voxy server", playerName);
			EventManager.callEvent(new VoxyClientConnected(this, true));
		} else {
			debug("A timeout or a connection lost happened after the client asks for server's properties");
			EventManager.callEvent(new VoxyClientConnected(this, false));
		}
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
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String message, Object... args) {
		Logger.info("%s - %s", client, String.format(message, args));
	}

	/**
	 * Print a log using DEBUG level.
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void debug(String format, Object... args) {
		Logger.debug("%s - %s", client, String.format(format, args));
	}
}
