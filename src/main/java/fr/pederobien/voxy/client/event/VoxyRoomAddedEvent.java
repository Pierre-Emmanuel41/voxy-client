package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoomAddedEvent extends VoxyRoomEvent {

	/**
	 * Event thrown when a voxy room has been added on the server.
	 * 
	 * @param room The added room.
	 */
	public VoxyRoomAddedEvent(IVoxyRoom room) {
		super(room);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("name=" + getRoom().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
