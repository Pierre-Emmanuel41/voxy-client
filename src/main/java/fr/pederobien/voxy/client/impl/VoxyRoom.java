package fr.pederobien.voxy.client.impl;

import java.util.Collections;
import java.util.Map;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyRoomJoinedEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRenamedEvent;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoom implements IVoxyRoom {
	private VoxyClient client;
	private String name;
	private int port;
	private Map<String, IVoxyPlayer> players;
	private Object lock;

	/**
	 * Creates a room that a player can join to speak with other player in this room.
	 * 
	 * @param client  The voxy client associated to this room.
	 * @param name    The room's name.
	 * @param port    The port number used to communicate with the room's server.
	 * @param players The map of players already in the room.
	 */
	public VoxyRoom(VoxyClient client, String name, int port, Map<String, IVoxyPlayer> players) {
		this.client = client;
		this.name = name;
		this.port = port;
		this.players = players;

		lock = new Object();
	}

	@Override
	public IVoxyClient getClient() {
		return client;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (this.name.equals(name))
			return;

		client.sendRoomRenameRequest(getName(), name);
	}

	@Override
	public Map<String, IVoxyPlayer> getPlayers() {
		return Collections.unmodifiableMap(players);
	}

	@Override
	public void join() {
		client.sendRoomJoinRequest(getName());
	}

	@Override
	public void leave() {

	}

	/**
	 * @return The address of the voxy server.
	 */
	protected String getAddress() {
		return client.getAddress();
	}

	/**
	 * @return The port number to use to communicate with the room's server.
	 */
	protected int getPort() {
		return port;
	}

	/**
	 * Called internally to update the room's name.
	 * 
	 * @param name The new room's name.
	 */
	protected void setNameInternal(String name) {
		if (this.name.equals(name))
			return;

		String oldName = this.name;
		this.name = name;

		info("Room %s has been renamed as %s", oldName, name);
		EventManager.callEvent(new VoxyRoomRenamedEvent(this, oldName));
	}

	/**
	 * Called internally to add a player to this room.
	 * 
	 * @param player The player that joined this room.
	 */
	protected void add(IVoxyPlayer player) {
		synchronized (lock) {
			if (players.get(player.getName()) != null)
				return;

			players.put(player.getName(), player);

			info("Player %s joined the room %s", player.getName(), getName());
			EventManager.callEvent(new VoxyRoomJoinedEvent(this, player));
		}
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
}
