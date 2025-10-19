package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoomLeftEvent extends VoxyRoomEvent {
	private final IVoxyPlayer player;

	/**
	 * Creates an event thrown when a player left a room.
	 * 
	 * @param room   The room a player left.
	 * @param player The player that left a room.
	 */
	public VoxyRoomLeftEvent(IVoxyRoom room, IVoxyPlayer player) {
		super(room);

		this.player = player;
	}

	/**
	 * @return The player that left a room.
	 */
	public IVoxyPlayer getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("room=" + getRoom().getName());
		joiner.add("player=" + getPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
