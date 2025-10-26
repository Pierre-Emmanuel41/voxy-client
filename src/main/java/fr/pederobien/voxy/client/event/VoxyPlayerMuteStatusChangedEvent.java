package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class VoxyPlayerMuteStatusChangedEvent extends VoxyPlayerEvent {
	private final boolean isMute;

	/**
	 * Creates an event thrown when the mute status of a player has changed.
	 * 
	 * @param player The player's whose the mute status has changed.
	 * @param isMute True if the player is muted, false otherwise.
	 */
	public VoxyPlayerMuteStatusChangedEvent(IVoxyPlayer player, boolean isMute) {
		super(player);

		this.isMute = isMute;
	}

	/**
	 * @return True if the player is muted, false otherwise.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("isMute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
