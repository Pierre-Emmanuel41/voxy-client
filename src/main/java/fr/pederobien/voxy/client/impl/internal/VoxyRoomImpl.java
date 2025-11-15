package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.client.event.VoxyRoomRenamedEvent;
import fr.pederobien.voxy.client.impl.VoxyRoom;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoomImpl extends ClientElement {
	private final PlayerListImpl playersImpl;
	private final int port;
	private String name;

	private final IVoxyRoom external;

	/**
	 * Creates the implementation of a voxy room.
	 * 
	 * @param client The client implementation associated to this room implementation.
	 * @param name   The room's name.
	 * @param port   The room's vocal server port number.
	 */
	protected VoxyRoomImpl(VoxyClientImpl client, String name, int port) {
		super(client);

		this.name = name;
		this.port = port;

		playersImpl = new PlayerListImpl(this);
		external = new VoxyRoom(this);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return The name of the room.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sends a request to the server to rename a room.
	 * 
	 * @param newName The room's new name.
	 */
	public void sendRoomRenameRequest(String newName) {
		getClient().getNotifier().sendRoomRenameRequest(name, newName);
	}

	/**
	 * Set the room's name.
	 *
	 * @param name The new room's name.
	 */
	public void setName(String name) {
		if (this.name.equals(name))
			return;

		String oldName = this.name;
		this.name = name;

		info("The room %s has been renamed as %s", oldName, name);
		EventManager.callEvent(new VoxyRoomRenamedEvent(external, oldName));
	}

	/**
	 * Sends a request to join a room on the server.
	 */
	public void sendRoomJoinRequest() {
		getClient().getNotifier().sendRoomJoinRequest(name);
	}

	/**
	 * Sends a request to leave a room from the server.
	 */
	public void sendRoomLeaveRequest() {
		getClient().getNotifier().sendRoomLeaveRequest(name);
	}

	/**
	 * @return The port number of the room's vocal server
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return The list of players registered in this room.
	 */
	public PlayerListImpl getPlayers() {
		return playersImpl;
	}

	/**
	 * @return The voxy room to use externally.
	 */
	public IVoxyRoom getExternal() {
		return external;
	}
}
