package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class VoxyPlayerMuteByStatusChangedEvent extends VoxyPlayerEvent {
	private final boolean isMute;

	/**
	 * Creates an event when the main player muted/unmuted a target player.
	 * 
	 * @param player The player that is muted/unmuted by the main player.
	 * @param isMute True if the target player is muted, false otherwise.
	 */
	public VoxyPlayerMuteByStatusChangedEvent(IVoxyPlayer player, boolean isMute) {
		super(player);

		this.isMute = isMute;
	}

	/**
	 * @return True if this target player muted, false otherwise.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("target=" + getPlayer());
		joiner.add("isMute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
