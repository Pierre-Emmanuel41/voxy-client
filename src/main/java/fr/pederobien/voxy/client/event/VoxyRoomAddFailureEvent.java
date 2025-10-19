package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.utils.event.Event;

public class VoxyRoomAddFailureEvent extends Event {
	private final String roomName;

	/**
	 * Creates an event thrown when the server denied to add a room.
	 * 
	 * @param roomName The name of the room to add.
	 */
	public VoxyRoomAddFailureEvent(String roomName) {
		this.roomName = roomName;
	}

	/**
	 * @return The name of the room to add.
	 */
	public String getRoomName() {
		return roomName;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("room=" + getRoomName());
		return String.format("%s_%s", getName(), joiner);
	}
}
