package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.client.IEthernetProtocolClientConfig;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.protocol.interfaces.IRequest;
import fr.pederobien.utils.event.Event;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.client.event.VoxyMainPlayerMuteStatusChangedEvent;
import fr.pederobien.voxy.client.event.VoxyPlayerMuteByFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomAddFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomJoinFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomLeaveFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRemoveFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRenameFailureEvent;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.JoinRoomRequest;
import fr.pederobien.voxy.common.impl.requests.LeaveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerDeafRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteByRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;

public class ServerNotifier extends ClientWrapper {
	private final VoxyMainPlayerImpl player;

	/**
	 * Creates a server notifier. This object is responsible to transmit client's request to the server.
	 * 
	 * @param client The client to use to send requests to the server.
	 * @param config The configuration to use to create requests / parse responses.
	 * @param player The main player
	 */
	public ServerNotifier(IProtocolClient client, IEthernetProtocolClientConfig config, VoxyMainPlayerImpl player) {
		super(client, config);

		this.player = player;
	}

	/**
	 * Sends a request to the server to add a room.
	 * 
	 * @param name The name of the room to add.
	 * @param port The port number to use for the room's vocal server.
	 */
	protected void sendRoomAddRequest(String name, int port) {
		info("Sending a request to the server to add room %s", name);
		IRequestMessage request = getRequest(VoxyIdentifiers.ADD_ROOM, new AddRoomRequest(name, port));
		request.setCallback(args -> accept(args, new VoxyRoomAddFailureEvent(name)));

		send(request);
	}

	/**
	 * Sends a request to the server to remove a room.
	 * 
	 * @param name The name of the room to remove.
	 */
	protected void sendRoomRemoveRequest(String name) {
		info("Sending a request to the server to remove room %s", name);
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
		info("Sending a request to the server to rename room %s as %s", oldName, newName);
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
		info("Sending a request to the server to join room %s", name);

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
		info("Sending a request to the server to leave room %s", name);

		// Disabling microphone and speakers before leaving a room
		player.getVocalClient().setMute(true);
		player.getVocalClient().setDeaf(true);

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

		info("Sending a request to the server to %s player %s", isMute ? "mute" : "unmute", name);

		// Main player updated its own mute status
		if (name.equals(player.getName())) {
			request = getRequest(VoxyIdentifiers.PLAYER_MUTE, new PlayerMuteRequest(name, isMute));
			request.setCallback(args -> accept(args, new VoxyMainPlayerMuteStatusChangedEvent(player.getExternal(), isMute)));
		}
		// Main player mutes/unmutes another player for itself
		else {
			request = getRequest(VoxyIdentifiers.PLAYER_MUTE_BY, new PlayerMuteByRequest(name, player.getName(), isMute));
			request.setCallback(args -> accept(args, new VoxyPlayerMuteByFailureEvent(player.getExternal(), name, isMute)));
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
		info("Sending a request to the server to update player %s's deaf status, isDeaf=%s", name, isDeaf);
		send(getRequest(VoxyIdentifiers.PLAYER_DEAF, new PlayerDeafRequest(name, isDeaf)));
	}

	/**
	 * Generic method to simplify source code reading.
	 * 
	 * @param args  The arguments that contains server's response.
	 * @param event The event to throw in case of a failure.
	 */
	private void accept(CallbackArgs args, Event event) {
		if (args.isTimeout() || args.isConnectionLost()) {
			info("A timeout occured or the connection has been lost");
			EventManager.callEvent(event);
		} else {
			IRequest response = getConfig().parse(args.response());
			if (response == null || response.getError() != VoxyErrors.NO_ERROR) {
				warning("The server denied the request: %s", response.getError().getMessage());
				EventManager.callEvent(event);
			}
		}
	}
}
