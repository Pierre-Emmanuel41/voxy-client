package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyClient;

public class VoxyClientConnectedEvent extends VoxyClientEvent {
	private final boolean success;

	/**
	 * Creates an event thrown when a Voxy client is connected to a Voxy server.
	 * 
	 * @param client  The connected client.
	 * @param success True if the client is connected, false otherwise.
	 */
	public VoxyClientConnectedEvent(IVoxyClient client, boolean success) {
		super(client);

		this.success = success;
	}

	/**
	 * @return True if the client is successfully connected, false otherwise.
	 */
	public boolean isSuccess() {
		return success;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("client=" + getClient());
		joiner.add("success=" + isSuccess());
		return String.format("%s_%s", getName(), joiner);
	}
}
