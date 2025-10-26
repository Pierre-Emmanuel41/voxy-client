package fr.pederobien.voxy.client.impl;

import java.util.HashMap;
import java.util.Map;

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
import fr.pederobien.utils.event.Event;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyClientConnectedEvent;
import fr.pederobien.voxy.client.event.VoxyPlayerMuteByFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomAddFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomJoinFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomLeaveFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRemoveFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRenameFailureEvent;
import fr.pederobien.voxy.client.interfaces.IRoomList;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.JoinRoomRequest;
import fr.pederobien.voxy.common.impl.requests.LeaveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerDeafRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteByRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.PlayerInfo;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.RoomInfo;

public class VoxyClient implements IVoxyClient, IEventListener {
	private final ProtocolClientConfig<IEthernetEndPoint> config;
	private final IProtocolClient client;
	private final VoxyMainPlayer player;
	private final VocalClient vocalClient;
	private final RoomList rooms;

	/**
	 * Creates a client to communicate with a voxy server.
	 *
	 * @param name    The player's name.
	 * @param address The server's address.
	 * @param port    The server's port number.
	 */
	protected VoxyClient(String name, String address, int port) {
		IEthernetEndPoint endPoint = new EthernetEndPoint(address, port);
		config = Messenger.createClientConfig(VoxyProtocolManager.instance(), name, endPoint);

		// Layer to communicate: RSA
		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		// Registering event handler
		config.addRequestHandler(VoxyIdentifiers.ADD_ROOM, this::onRoomAdded);
		config.addRequestHandler(VoxyIdentifiers.REMOVE_ROOM, this::onRoomRemoved);
		config.addRequestHandler(VoxyIdentifiers.RENAME_ROOM, this::onRoomRenamed);
		config.addRequestHandler(VoxyIdentifiers.PLAYER_PROPERTIES, this::onPlayerProperties);
		config.addRequestHandler(VoxyIdentifiers.JOIN_ROOM, this::onPlayerJoinedRoom);
		config.addRequestHandler(VoxyIdentifiers.LEAVE_ROOM, this::onPlayerLeftRoom);
		config.addRequestHandler(VoxyIdentifiers.PLAYER_MUTE, this::onPlayerMuteStatusChanged);
		config.addRequestHandler(VoxyIdentifiers.PLAYER_DEAF, this::onPlayerDeafStatusChanged);

		client = Messenger.createTcpClient(config);

		player = new VoxyMainPlayer(this, name);
		vocalClient = new VocalClient(player);
		rooms = new RoomList(this);

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
	public IRoomList getRooms() {
		return rooms;
	}

	@Override
	public IVoxyMainPlayer getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return client.toString();
	}

	/**
	 * @return The address of the voxy server.
	 */
	protected String getAddress() {
		return config.getEndPoint().getAddress();
	}

	/**
	 * Sends a request to the server to add a room.
	 * 
	 * @param name The name of the room to add.
	 */
	protected void sendRoomAddRequest(String name) {
		debug("Sending a request to the server to add room %s", name);
		IRequestMessage request = getRequest(VoxyIdentifiers.ADD_ROOM, new AddRoomRequest(name, 0));
		request.setCallback(args -> accept(args, new VoxyRoomAddFailureEvent(name)));

		send(request);
	}

	/**
	 * Sends a request to the server to remove a room.
	 * 
	 * @param name The name of the room to remove.
	 */
	protected void sendRoomRemoveRequest(String name) {
		debug("Sending a request to the server to remove room %s", name);
		IRequestMessage request = getRequest(VoxyIdentifiers.REMOVE_ROOM, new RemoveRoomRequest(name));
		request.setCallback(args -> accept(args, new VoxyRoomRemoveFailureEvent(name)));

		send(request);
	}

	/**
	 * Sends a request to the server to rename a room.
	 * 
	 * @param oldName The name of the room to rename.
	 * @param newName The room's new name.
	 */
	protected void sendRoomRenameRequest(String oldName, String newName) {
		debug("Sending a request to the server to rename room %s as %s", oldName, newName);
		IRequestMessage request = getRequest(VoxyIdentifiers.RENAME_ROOM, new RenameRoomRequest(oldName, newName));
		request.setCallback(args -> accept(args, new VoxyRoomRenameFailureEvent(oldName, newName)));

		send(request);
	}

	/**
	 * Sends a request to join a room on the server.
	 * 
	 * @param name The name of the room to join.
	 */
	protected void sendRoomJoinRequest(String name) {
		debug("Sending a request to the server to join room %s", name);

		JoinRoomRequest payload = new JoinRoomRequest(name, player.getName(), player.isMute(), player.isDeaf());
		IRequestMessage request = getRequest(VoxyIdentifiers.JOIN_ROOM, payload);
		request.setCallback(args -> accept(args, new VoxyRoomJoinFailureEvent(name)));

		send(request);
	}

	/**
	 * Sends a request to leave a room on the server.
	 * 
	 * @param name The name of the room to leave.
	 */
	protected void sendRoomLeaveRequest(String name) {
		debug("Sending a request to the server to leave room %s", name);

		IRequestMessage request = getRequest(VoxyIdentifiers.LEAVE_ROOM, new LeaveRoomRequest(name, player.getName()));
		request.setCallback(args -> accept(args, new VoxyRoomLeaveFailureEvent(name)));

		send(request);
	}

	/**
	 * Sends a request to the server that player's mute status has changed.
	 * 
	 * @param name   The name of the player whose the mute status has changed.
	 * @param isMute True if the player is muted, false if the player is unmuted.
	 */
	protected void sendPlayerMuteStatusChanged(String name, boolean isMute) {
		IRequestMessage request;

		// Main player updated its own mute status
		if (name.equals(player.getName())) {
			debug("Sending a request to the server to update player %s's mute status, isMute=%s", name, isMute);
			request = getRequest(VoxyIdentifiers.PLAYER_MUTE, new PlayerMuteRequest(name, isMute));
		}
		// Main player mutes/unmutes another player for itself
		else {
			debug("Sending a request to the server to update player %s's mute status for %s, isMute=%s", name, player.getName(), isMute);
			request = getRequest(VoxyIdentifiers.PLAYER_MUTE_BY, new PlayerMuteByRequest(name, player.getName(), isMute));
			request.setCallback(args -> accept(args, new VoxyPlayerMuteByFailureEvent(player, name, isMute)));
		}

		send(request);
	}

	/**
	 * Sends a request to the server that player's deaf status has changed.
	 * 
	 * @param name   The name of the player whose the deaf status has changed.
	 * @param isDeaf True if the player is deaf, false if the player is undeaf.
	 */
	protected void sendPlayerDeafStatusChanged(String name, boolean isDeaf) {
		debug("Sending a request to the server to update player %s's deaf status, isDeaf=%s", name, isDeaf);
		send(getRequest(VoxyIdentifiers.PLAYER_DEAF, new PlayerDeafRequest(name, isDeaf)));
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
		rooms.add(new VoxyRoom(this, request.getName(), request.getPort(), new HashMap<String, IVoxyPlayer>()));
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

		VoxyRoom room = rooms.getByName(request.getName());
		if (room != null)
			rooms.remove(room);
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

		VoxyRoom room = rooms.getByName(request.getOldName());
		if (room != null)
			room.setNameInternal(request.getNewName());
	}

	/**
	 * Event handler: Method called when the server notify the client that a player joined a room.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the room and the player.
	 */
	private void onPlayerJoinedRoom(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof JoinRoomRequest request))
			return;

		debug("Receiving request that player %s joined room %s", request.getPlayerName(), request.getRoomName());
		VoxyRoom room = rooms.getByName(request.getRoomName());

		if (room == null) {
			error("Technical error: No existing room for %s", request.getRoomName());
			EventManager.callEvent(new VoxyRoomJoinFailureEvent(request.getRoomName()));
			return;
		}

		// Specific sequence : Connecting vocal client
		if (request.getPlayerName().equals(player.getName()))
			vocalClient.connect(room);
		else
			room.add(new VoxyPlayer(this, request.getPlayerName(), request.isMute(), request.isDeaf()));
	}

	/**
	 * Event handler: Method called when the server notify the client that a player left a room.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the room and the player.
	 */
	private void onPlayerLeftRoom(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof LeaveRoomRequest request))
			return;

		debug("Receiving request that player %s left room %s", request.getPlayerName(), request.getRoomName());
		VoxyRoom room = rooms.getByName(request.getRoomName());

		if (room == null) {
			error("Technical error: No existing room for %s", request.getRoomName());
			EventManager.callEvent(new VoxyRoomLeaveFailureEvent(request.getRoomName()));
			return;
		}

		// Specific sequence : Connecting vocal client
		if (request.getPlayerName().equals(player.getName()))
			vocalClient.disconnect();
		else
			room.remove(request.getPlayerName());
	}

	/**
	 * Event handler: Method called when the server notify the client that player's mute status has changed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the player and the mute status.
	 */
	private void onPlayerMuteStatusChanged(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerMuteRequest request))
			return;

		debug("Receiving request that the mute status of player %s has changed, isMute=%s", request.getName(), request.isMute());

		for (IVoxyRoom room : rooms.toList()) {
			IVoxyPlayer player = room.getPlayers().get(request.getName());

			if (player != null) {
				VoxyPlayer cast = (VoxyPlayer) player;
				cast.setMuteInternal(request.isMute());
			}
		}
	}

	/**
	 * Event handler: Method called when the server notify the client that player's deaf status has changed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the player and the deaf status.
	 */
	private void onPlayerDeafStatusChanged(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerDeafRequest request))
			return;

		debug("Receiving request that the deaf status of player %s has changed, isDeaf=%s", request.getName(), request.isDeaf());

		for (IVoxyRoom room : rooms.toList()) {
			IVoxyPlayer player = room.getPlayers().get(request.getName());

			if (player != null) {
				VoxyPlayer cast = (VoxyPlayer) player;
				cast.setDeafInternal(request.isDeaf());
			}
		}
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been added.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 */
	private void onPlayerProperties(IProtocolConnection connection, int messageID, Object ignored) {
		debug("Server requires player's properties");
		PlayerPropertiesRequest payload = new PlayerPropertiesRequest(player.getName(), player.isMute(), player.isDeaf());

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
				EventManager.callEvent(new VoxyClientConnectedEvent(this, false));
				return;
			}

			if (response.getIdentifier() != VoxyIdentifiers.ACKOWLEDGEMENT) {
				debug("The server did not acknowledge back player's properties");
				EventManager.callEvent(new VoxyClientConnectedEvent(this, false));
				return;
			}

			if (response.getError() != VoxyErrors.NO_ERROR) {
				debug("The server did not accept player properties: %s", response.getError().getMessage());
				EventManager.callEvent(new VoxyClientConnectedEvent(this, false));
				return;
			}

			// Sending request to get server's properties
			debug("Requiring server's properties");
			IRequestMessage request = getRequest(VoxyIdentifiers.SERVER_PROPERTIES, new ServerPropertiesRequest());
			request.setCallback(args -> handleServerProperties(args));
			send(request);

		} else {
			debug("A timeout or a connection lost happened after the client sent player's properties");
			EventManager.callEvent(new VoxyClientConnectedEvent(this, false));
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
				EventManager.callEvent(new VoxyClientConnectedEvent(this, false));
				return;
			}

			if (response.getError() != VoxyErrors.NO_ERROR) {
				debug("The server did not accept server's properties: %s", response.getError().getMessage());
				EventManager.callEvent(new VoxyClientConnectedEvent(this, false));
				return;
			}

			ServerPropertiesRequest serverProperties = (ServerPropertiesRequest) response.getPayload();
			for (RoomInfo roomInfo : serverProperties.getRooms()) {

				Map<String, IVoxyPlayer> players = new HashMap<String, IVoxyPlayer>();
				for (PlayerInfo playerInfo : roomInfo.players())
					players.put(playerInfo.name(), new VoxyPlayer(this, playerInfo.name(), playerInfo.isMute(), playerInfo.isDeaf()));

				rooms.add(new VoxyRoom(this, roomInfo.name(), roomInfo.port(), players));
			}

			info("%s successfully joined the voxy server", player.getName());
			EventManager.callEvent(new VoxyClientConnectedEvent(this, true));
		} else {
			debug("A timeout or a connection lost happened after the client asks for server's properties");
			EventManager.callEvent(new VoxyClientConnectedEvent(this, false));
		}
	}

	/**
	 * Generic method to simplify source code reading.
	 * 
	 * @param args     The arguments that contains server's response.
	 * @param callback The action to execute.
	 */
	private void accept(CallbackArgs args, Event event) {
		if (args.isTimeout() || args.isConnectionLost())
			EventManager.callEvent(event);
		else {
			IRequest response = config.parse(args.response());
			if (response == null || response.getError() != VoxyErrors.NO_ERROR)
				EventManager.callEvent(event);
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
	private void info(String message, Object... args) {
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

	/**
	 * Print a log using ERROR level.
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void error(String format, Object... args) {
		Logger.error("%s - %s", client, String.format(format, args));
	}
}
