package fr.pederobien.voxy.client.event;

import fr.pederobien.utils.event.Event;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class VoxyPlayerEvent extends Event {
	private final IVoxyPlayer player;

	/**
	 * Creates a voxy player event.
	 * 
	 * @param player The player involved in this event.
	 */
	public VoxyPlayerEvent(IVoxyPlayer player) {
		this.player = player;
	}

	/**
	 * @return The player involved in this event.
	 */
	public IVoxyPlayer getPlayer() {
		return player;
	}
}
