package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyPlayerMuteByFailureEvent extends VoxyMainPlayerEvent {
	private final String target;
	private final boolean isMute;

	/**
	 * Creates an event when an error happened while muting/unmuting a player for the main player.
	 * 
	 * @param player The player that mutes/unmutes another player.
	 * @param target The player to mute/unmute.
	 * @param isMute True if the target player shall be muted, false otherwise.
	 */
	public VoxyPlayerMuteByFailureEvent(IVoxyMainPlayer player, String target, boolean isMute) {
		super(player);

		this.target = target;
		this.isMute = isMute;
	}

	/**
	 * @return The player to mute/unmute.
	 */
	public String getTarget() {
		return target;
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
		joiner.add("target=" + getTarget());
		joiner.add("isMute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
