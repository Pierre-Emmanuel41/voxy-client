package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.utils.event.Event;

public class VoxyRoomRenameFailureEvent extends Event {
	private final String oldName;
	private final String newName;

	/**
	 * Creates an event thrown when the server denied to rename a room.
	 * 
	 * @param roomName The name of the room to rename.
	 * @param newName  The room's new name.
	 */
	public VoxyRoomRenameFailureEvent(String oldName, String newName) {
		this.oldName = oldName;
		this.newName = newName;
	}

	/**
	 * @return The name of the room to rename.
	 */
	public String getOldName() {
		return oldName;
	}

	/**
	 * @return The room's new name.
	 */
	public String getNewName() {
		return newName;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("oldName=" + getOldName());
		joiner.add("newName=" + getNewName());
		return String.format("%s_%s", getName(), joiner);
	}
}
