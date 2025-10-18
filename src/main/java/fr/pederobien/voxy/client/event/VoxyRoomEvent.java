package fr.pederobien.voxy.client.event;

import fr.pederobien.utils.event.Event;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoomEvent extends Event {
	private final IVoxyRoom room;

	/**
	 * Creates a voxy room event.
	 * 
	 * @param room The room involved in this event.
	 */
	public VoxyRoomEvent(IVoxyRoom room) {
		this.room = room;
	}

	/**
	 * @return The room involved in this event.
	 */
	public IVoxyRoom getRoom() {
		return room;
	}
}
