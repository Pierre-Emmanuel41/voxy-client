package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyMainPlayerMuteFailureEvent extends VoxyMainPlayerEvent {
	private final boolean isMute;

	/**
	 * Creates an event when an error happened while unmuting the main player.
	 * 
	 * @param player The player that mutes/unmutes another player.
	 * @param isMute True if the target player shall be muted, false otherwise.
	 */
	public VoxyMainPlayerMuteFailureEvent(IVoxyMainPlayer player, boolean isMute) {
		super(player);

		this.isMute = isMute;
	}

	/**
	 * @return True if the target player shall be muted, false otherwise.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("source=" + getPlayer().getName());
		joiner.add("isMute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
