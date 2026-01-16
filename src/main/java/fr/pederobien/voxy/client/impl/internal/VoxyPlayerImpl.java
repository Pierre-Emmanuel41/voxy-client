package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.client.event.VoxyPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.client.event.VoxyPlayerMuteByStatusChangedEvent;
import fr.pederobien.voxy.client.event.VoxyPlayerMuteStatusChangedEvent;
import fr.pederobien.voxy.client.impl.VoxyPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class VoxyPlayerImpl extends ClientElement {
	private final String name;
	private boolean isMute;
	private boolean isMuteByMainPlayer;
	private boolean isDeaf;
	private final IVoxyPlayer external;

	/**
	 * Creates an implementation of a voxy player.
	 * 
	 * @param client The client implementation associated to this player.
	 * @param name   The player's name.
	 * @param isMute The player's mute status.
	 * @param isDeaf The player's deaf status.
	 */
	protected VoxyPlayerImpl(VoxyClientImpl client, String name, boolean isMute, boolean isDeaf) {
		super(client);

		this.name = name;
		this.isMute = isMute;
		this.isDeaf = isDeaf;

		isMuteByMainPlayer = false;
		external = new VoxyPlayer(this);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return The player's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return True if the player is muted, false otherwise.
	 */
	public boolean isMute() {
		return isMute;
	}

	/**
	 * Sends a request to the server that player's mute status has changed.
	 * 
	 * @param isMute True if the player is muted, false if the player is unmuted.
	 */
	public void sendPlayerMuteStatusChanged(boolean isMuteByMainPlayer) {
		if (this.isMuteByMainPlayer == isMuteByMainPlayer)
			return;

		debug("Player %s required to %s player %s", getClient().getPlayer().getName(), isMute ? "mute" : "unmute", name);
		getClient().getNotifier().sendPlayerMuteStatusChanged(name, isMute);
	}

	/**
	 * Mute this player only for this client.
	 *
	 * @param isMute True to mute the player, false to unmute.
	 */
	public void setMute(boolean isMute) {
		if (this.isMute == isMute)
			return;

		this.isMute = isMute;

		if (!name.equals(getClient().getPlayer().getName())) {
			info("Player %s %s itself", name, isMute ? "muted" : "unmuted");
			EventManager.callEvent(new VoxyPlayerMuteStatusChangedEvent(external, isMute));
		}
	}

	/**
	 * @return True if this player is muted by the main player, false otherwise.
	 */
	public boolean isMuteByMainPlayer() {
		return isMuteByMainPlayer;
	}

	/**
	 * Set if the main player mutes or unmutes this player.
	 * 
	 * @param isMuteByMainPlayer True if the main player mutes this player, false otherwise.
	 */
	public void setMuteByMainPlayer(boolean isMuteByMainPlayer) {
		if (this.isMuteByMainPlayer == isMuteByMainPlayer)
			return;

		this.isMuteByMainPlayer = isMuteByMainPlayer;
		info("Player %s %s player %s", getClient().getPlayer().getName(), isMute ? "muted" : "unmuted", name);
		EventManager.callEvent(new VoxyPlayerMuteByStatusChangedEvent(external, isMuteByMainPlayer));
	}

	/**
	 * @return True if the player disabled it speakers, false otherwise.
	 */
	public boolean isDeaf() {
		return isDeaf;
	}

	/**
	 * Set if this player is deaf or undeaf.
	 *
	 * @param isDeaf True if the player disabled its speakers, false if he enabled them.
	 */
	public void setDeaf(boolean isDeaf) {
		if (this.isDeaf == isDeaf)
			return;

		this.isDeaf = isDeaf;

		info("Player %s %s itself", name, isDeaf ? "deaf" : "undeaf");
		EventManager.callEvent(new VoxyPlayerDeafStatusChangedEvent(external, isDeaf));
	}

	/**
	 * @return The player to use externally.
	 */
	public IVoxyPlayer getExternal() {
		return external;
	}
}
