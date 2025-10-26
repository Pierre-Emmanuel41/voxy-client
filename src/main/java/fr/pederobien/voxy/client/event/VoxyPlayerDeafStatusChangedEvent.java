package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class VoxyPlayerDeafStatusChangedEvent extends VoxyPlayerEvent {
	private final boolean isDeaf;

	/**
	 * Creates an event thrown when the deaf status of a player has changed.
	 * 
	 * @param player The player's whose the deaf status has changed.
	 * @param isDeaf True if the player is deaf, false otherwise.
	 */
	public VoxyPlayerDeafStatusChangedEvent(IVoxyPlayer player, boolean isDeaf) {
		super(player);

		this.isDeaf = isDeaf;
	}

	/**
	 * @return True if the player is deaf, false otherwise.
	 */
	public boolean isDeaf() {
		return isDeaf;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("isDeaf=" + isDeaf());
		return String.format("%s_%s", getName(), joiner);
	}
}
