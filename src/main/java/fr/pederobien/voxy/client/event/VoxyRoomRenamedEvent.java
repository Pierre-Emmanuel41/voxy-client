package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoomRenamedEvent extends VoxyRoomEvent {
	private final String oldName;

	/**
	 * Creates an event thrown when a voxy room has been renamed
	 * 
	 * @param room    The room that has been renamed.
	 * @param oldName The old room's name.
	 */
	public VoxyRoomRenamedEvent(IVoxyRoom room, String oldName) {
		super(room);

		this.oldName = oldName;
	}

	/**
	 * @return The old room's name.
	 */
	public String getOldName() {
		return oldName;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("oldName=" + getOldName());
		joiner.add("newName=" + getRoom().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
