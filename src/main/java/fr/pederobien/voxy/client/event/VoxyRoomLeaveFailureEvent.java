package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.utils.event.Event;

public class VoxyRoomLeaveFailureEvent extends Event {
	private final String roomName;

	/**
	 * Creates an event thrown when the server denied to leave a room.
	 * 
	 * @param roomName The name of the room to leave.
	 */
	public VoxyRoomLeaveFailureEvent(String roomName) {
		this.roomName = roomName;
	}

	/**
	 * @return The name of the room to leave.
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
