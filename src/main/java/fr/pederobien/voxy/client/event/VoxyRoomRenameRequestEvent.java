package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;
import java.util.function.Consumer;

import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoomRenameRequestEvent extends VoxyRoomEvent {
	private final String newName;
	private final Consumer<Boolean> callback;

	/**
	 * Creates an event thrown when a room shall be renamed.
	 * 
	 * @param room     The room to rename.
	 * @param newName  The room's new name.
	 * @param callback The action to execute when a response from the server has been received.
	 */
	public VoxyRoomRenameRequestEvent(IVoxyRoom room, String newName, Consumer<Boolean> callback) {
		super(room);

		this.newName = newName;
		this.callback = callback;
	}

	/**
	 * @return The room's new name.
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @return The action to execute when a response from the server has been received.
	 */
	public Consumer<Boolean> getCallback() {
		return callback;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("oldName=" + getRoom().getName());
		joiner.add("newName=" + getNewName());
		return String.format("%s_%s", getName(), joiner);
	}
}
