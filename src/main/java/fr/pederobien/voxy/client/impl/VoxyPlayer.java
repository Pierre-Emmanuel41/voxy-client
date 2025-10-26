package fr.pederobien.voxy.client.impl;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.client.event.VoxyPlayerMuteStatusChangedEvent;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class VoxyPlayer implements IVoxyPlayer {
	private VoxyClient client;
	private String name;
	private boolean isMute;
	private boolean isDeaf;

	/**
	 * Creates a voxy player.
	 * 
	 * @param client The voxy client associated to the player.
	 * @param name   The player's name.
	 * @param isMute The player's mute status.
	 * @param isDeaf The player's deaf status.
	 */
	public VoxyPlayer(VoxyClient client, String name, boolean isMute, boolean isDeaf) {
		this.client = client;
		this.name = name;
		this.isMute = isMute;
		this.isDeaf = isDeaf;
	}

	/**
	 * Creates a voxy player.
	 * 
	 * @param player The voxy application main player.
	 */
	public VoxyPlayer(VoxyMainPlayer player) {
		this.client = player.getClient();
		this.name = player.getName();
		this.isMute = player.isMute();
		this.isDeaf = player.isDeaf();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isMute() {
		return isMute;
	}

	@Override
	public void setMute(boolean isMute) {
		if (this.isMute == isMute)
			return;

		client.sendPlayerMuteStatusChanged(name, isMute);
	}

	@Override
	public boolean isDeaf() {
		return isDeaf;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Method called internally when the mute status has changed.
	 * 
	 * @param isMute True if the player is muted, false otherwise.
	 */
	protected void setMuteInternal(boolean isMute) {
		if (this.isMute == isMute)
			return;

		this.isMute = isMute;

		info("%s itself", isMute ? "Muted" : "Unmuted");
		EventManager.callEvent(new VoxyPlayerMuteStatusChangedEvent(this, isMute));
	}

	/**
	 * Method called internally when the deaf status has changed.
	 * 
	 * @param isDeaf True if the player is deaf, false otherwise.
	 */
	protected void setDeafInternal(boolean isDeaf) {
		if (this.isDeaf == isDeaf)
			return;

		this.isDeaf = isDeaf;

		info("%s itself", isDeaf ? "Muted" : "Unmuted");
		EventManager.callEvent(new VoxyPlayerDeafStatusChangedEvent(this, isDeaf));
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String message, Object... args) {
		Logger.info("%s - %s", this, String.format(message, args));
	}
}
