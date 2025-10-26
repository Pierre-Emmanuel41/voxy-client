package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyMainPlayerMuteStatusChangedEvent extends VoxyMainPlayerEvent {
	private final boolean isMute;

	/**
	 * Creates an event thrown when the mute status of a player has changed.
	 * 
	 * @param player The player whose the mute status has changed.
	 * @param isMute The player's current mute status.
	 */
	public VoxyMainPlayerMuteStatusChangedEvent(IVoxyMainPlayer player, boolean isMute) {
		super(player);

		this.isMute = isMute;
	}

	/**
	 * @return The player's current mute status. True if the player is mute, false otherwise.
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
