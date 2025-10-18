package fr.pederobien.voxy.client.impl;

import java.util.Collections;
import java.util.Map;

import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoom implements IVoxyRoom {
	private VoxyClient client;
	private String name;
	private int port;
	private Map<String, IVoxyPlayer> players;

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
		if (this.name == name)
			return;

		// TODO: Send a request to the server to rename a room
	}

	@Override
	public Map<String, IVoxyPlayer> getPlayers() {
		return Collections.unmodifiableMap(players);
	}
}
