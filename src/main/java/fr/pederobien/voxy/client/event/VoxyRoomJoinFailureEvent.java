package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.utils.event.Event;

public class VoxyRoomJoinFailureEvent extends Event {
	private final String roomName;

	/**
	 * Creates an event thrown when an error happened while joining a voxy room.
	 * 
	 * @param roomName The name of the room to join.
	 */
	public VoxyRoomJoinFailureEvent(String roomName) {
		this.roomName = roomName;
	}

	/**
	 * @return The name of the room to join.
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
