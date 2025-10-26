package fr.pederobien.voxy.client.impl;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.client.event.VoxyMainPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.client.event.VoxyMainPlayerMuteStatusChangedEvent;
import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyMainPlayer implements IVoxyMainPlayer {
	private final VoxyClient voxyClient;
	private final String name;
	private boolean isMute;
	private boolean isDeaf;

	/**
	 * Creates the main player of the voxy application.
	 * 
	 * @param client The client to use to send requests to the server.
	 * @param name   The player's name.
	 */
	public VoxyMainPlayer(VoxyClient client, String name) {
		this.voxyClient = client;
		this.name = name;

		isMute = true;
		isDeaf = true;
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

		voxyClient.sendPlayerMuteStatusChanged(name, isMute);
		this.isMute = isMute;
		EventManager.callEvent(new VoxyMainPlayerMuteStatusChangedEvent(this, isMute));
	}

	@Override
	public boolean isDeaf() {
		return isDeaf;
	}

	@Override
	public void setDeaf(boolean isDeaf) {
		if (this.isDeaf == isDeaf)
			return;

		voxyClient.sendPlayerDeafStatusChanged(name, isDeaf);
		this.isDeaf = isDeaf;
		EventManager.callEvent(new VoxyMainPlayerDeafStatusChangedEvent(this, isDeaf));
	}

	/**
	 * @return The client associated to this player.
	 */
	protected VoxyClient getClient() {
		return voxyClient;
	}
}
