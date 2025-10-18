package fr.pederobien.voxy.client.event;

import fr.pederobien.utils.event.Event;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;

public class VoxyClientEvent extends Event {
	private final IVoxyClient client;

	/**
	 * Creates a Voxy client event.
	 * 
	 * @param client The Voxy client involved in this event.
	 */
	public VoxyClientEvent(IVoxyClient client) {
		this.client = client;
	}

	/**
	 * @return The Voxy client involved in this event.
	 */
	public IVoxyClient getClient() {
		return client;
	}
}
