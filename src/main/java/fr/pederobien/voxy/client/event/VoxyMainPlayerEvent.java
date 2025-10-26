package fr.pederobien.voxy.client.event;

import fr.pederobien.utils.event.Event;
import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyMainPlayerEvent extends Event {
	private final IVoxyMainPlayer player;

	/**
	 * Creates a voxy main player event.
	 * 
	 * @param player The player involved in this event.
	 */
	public VoxyMainPlayerEvent(IVoxyMainPlayer player) {
		this.player = player;
	}

	/**
	 * @return The player involved in this event.
	 */
	public IVoxyMainPlayer getPlayer() {
		return player;
	}
}
