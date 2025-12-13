package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.protocol.interfaces.IRequest;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyClientConnectedEvent;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.JoinRoomRequest;
import fr.pederobien.voxy.common.impl.requests.LeaveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerDeafRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.PlayerInfo;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.RoomInfo;

public class ServerRequestHandler extends ClientWrapper {
	private final VoxyClientImpl client;
	private boolean isEnabled;

	/**
	 * Creates a request handler associated to a client.
	 * 
	 * @param client The implementation of a voxy client.
	 */
	public ServerRequestHandler(VoxyClientImpl client) {
		super(client.getClient(), client.getConfig());

		this.client = client;

		// Registering event handler
		getConfig().addRequestHandler(VoxyIdentifiers.ADD_ROOM, this::onRoomAdded);
		getConfig().addRequestHandler(VoxyIdentifiers.REMOVE_ROOM, this::onRoomRemoved);
		getConfig().addRequestHandler(VoxyIdentifiers.RENAME_ROOM, this::onRoomRenamed);
		getConfig().addRequestHandler(VoxyIdentifiers.PLAYER_PROPERTIES, this::onPlayerProperties);
		getConfig().addRequestHandler(VoxyIdentifiers.JOIN_ROOM, this::onPlayerJoinedRoom);
		getConfig().addRequestHandler(VoxyIdentifiers.LEAVE_ROOM, this::onPlayerLeftRoom);
		getConfig().addRequestHandler(VoxyIdentifiers.PLAYER_MUTE, this::onPlayerMuteStatusChanged);
		getConfig().addRequestHandler(VoxyIdentifiers.PLAYER_DEAF, this::onPlayerDeafStatusChanged);

		isEnabled = false;
	}

	/**
	 * Set if this handler shall executed request handler when receiving a request from the server.
	 * 
	 * @param isEnabled True to enable handlers execution, false otherwise.
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been added.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the new room.
	 */
	private void onRoomAdded(IProtocolConnection connection, int messageID, Object payload) {
		if (!isEnabled || !(payload instanceof AddRoomRequest request))
			return;

		debug("Receiving request to add room %s", request.getName());
		getRooms().add(request.getName(), request.getPort());
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been removed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the removed room.
	 */
	private void onRoomRemoved(IProtocolConnection connection, int messageID, Object payload) {
		if (!isEnabled || !(payload instanceof RemoveRoomRequest request))
			return;

		debug("Receiving request to remove room %s", request.getName());

		VoxyRoomImpl room = getRooms().getByName(request.getName());
		if (room != null)
			getRooms().remove(room);
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been renamed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the renamed room.
	 */
	private void onRoomRenamed(IProtocolConnection connection, int messageID, Object payload) {
		if (!isEnabled || !(payload instanceof RenameRoomRequest request))
			return;

		debug("Receiving request to rename room %s as %s", request.getOldName(), request.getNewName());

		VoxyRoomImpl room = getRooms().getByName(request.getOldName());
		if (room != null)
			room.setName(request.getNewName());
	}

	/**
	 * Event handler: Method called when the server notify the client that a player joined a room.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the room and the player.
	 */
	private void onPlayerJoinedRoom(IProtocolConnection connection, int messageID, Object payload) {
		if (!isEnabled || !(payload instanceof JoinRoomRequest request))
			return;

		debug("Receiving request that player %s joined room %s", request.getPlayerName(), request.getRoomName());
		VoxyRoomImpl room = getRooms().getByName(request.getRoomName());

		if (room != null)
			room.getPlayers().add(new VoxyPlayerImpl(client, request.getPlayerName(), request.isMute(), request.isDeaf()));
	}

	/**
	 * Event handler: Method called when the server notify the client that a player left a room.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the room and the player.
	 */
	private void onPlayerLeftRoom(IProtocolConnection connection, int messageID, Object payload) {
		if (!isEnabled || !(payload instanceof LeaveRoomRequest request))
			return;

		debug("Receiving request that player %s left room %s", request.getPlayerName(), request.getRoomName());
		VoxyRoomImpl room = getRooms().getByName(request.getRoomName());

		if (room != null) {
			VoxyPlayerImpl player = room.getPlayers().getByName(request.getPlayerName());
			if (player != null)
				room.getPlayers().remove(player);
		}
	}

	/**
	 * Event handler: Method called when the server notify the client that player's mute status has changed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the player and the mute status.
	 */
	private void onPlayerMuteStatusChanged(IProtocolConnection connection, int messageID, Object payload) {
		if (!isEnabled || !(payload instanceof PlayerMuteRequest request))
			return;

		debug("Receiving request that the mute status of player %s has changed, isMute=%s", request.getName(), request.isMute());

		getRooms().foreach(room -> {
			VoxyPlayerImpl player = room.getPlayers().getByName(request.getName());
			if (player != null)
				player.setMute(request.isMute());
		});
	}

	/**
	 * Event handler: Method called when the server notify the client that player's deaf status has changed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The object that gather properties about the player and the deaf status.
	 */
	private void onPlayerDeafStatusChanged(IProtocolConnection connection, int messageID, Object payload) {
		if (!isEnabled || !(payload instanceof PlayerDeafRequest request))
			return;

		debug("Receiving request that the deaf status of player %s has changed, isDeaf=%s", request.getName(), request.isDeaf());

		getRooms().foreach(room -> {
			VoxyPlayerImpl player = room.getPlayers().getByName(request.getName());
			if (player != null)
				player.setDeaf(request.isDeaf());
		});
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been added.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 */
	private void onPlayerProperties(IProtocolConnection connection, int messageID, Object ignored) {
		debug("Server requires player's properties");
		PlayerPropertiesRequest payload = new PlayerPropertiesRequest(getPlayer().getName(), getPlayer().isMute(), getPlayer().isDeaf());

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
			IRequest response = getConfig().parse(argument.response());

			if (response == null) {
				Logger.error("Technical error happened: Could not parse server's response for player's properties");
				EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), false));
				return;
			}

			if (response.getIdentifier() != VoxyIdentifiers.ACKNOWLEDGEMENT) {
				debug("The server did not acknowledge back player's properties");
				EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), false));
				return;
			}

			if (response.getError() != VoxyErrors.NO_ERROR) {
				debug("The server did not accept player properties: %s", response.getError().getMessage());
				EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), false));
				return;
			}

			// Sending request to get server's properties
			debug("Requiring server's properties");
			IRequestMessage request = getRequest(VoxyIdentifiers.SERVER_PROPERTIES, new ServerPropertiesRequest());
			request.setCallback(args -> handleServerProperties(args));
			send(request);

		} else {
			debug("A timeout or a connection lost happened after the client sent player's properties");
			EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), false));
		}
	}

	/**
	 * Method called when the server sends it properties.
	 *
	 * @param argument The argument that contains server's response.
	 */
	private void handleServerProperties(CallbackArgs argument) {
		if (!(argument.isTimeout() || argument.isConnectionLost())) {
			IRequest response = getConfig().parse(argument.response());

			if (response == null) {
				Logger.error("Technical error happened: Could not parse server's response for server's properties");
				EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), false));
				return;
			}

			if (response.getError() != VoxyErrors.NO_ERROR) {
				debug("The server did not accept server's properties: %s", response.getError().getMessage());
				EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), false));
				return;
			}

			ServerPropertiesRequest serverProperties = (ServerPropertiesRequest) response.getPayload();
			for (RoomInfo roomInfo : serverProperties.getRooms()) {

				VoxyRoomImpl room = getRooms().add(roomInfo.name(), roomInfo.port());
				for (PlayerInfo playerInfo : roomInfo.players())
					room.getPlayers().add(new VoxyPlayerImpl(client, playerInfo.name(), playerInfo.isMute(), playerInfo.isDeaf()));
			}

			info("%s successfully joined the voxy server", getPlayer().getName());
			EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), true));
		} else {
			debug("A timeout or a connection lost happened after the client asks for server's properties");
			EventManager.callEvent(new VoxyClientConnectedEvent(client.getExternal(), false));
		}
	}

	/**
	 * @return The implementation of a rooms list.
	 */
	private RoomListImpl getRooms() {
		return client.getRooms();
	}

	/**
	 * @return The implementation of a voxy main player.
	 */
	private VoxyMainPlayerImpl getPlayer() {
		return client.getPlayer();
	}
}
