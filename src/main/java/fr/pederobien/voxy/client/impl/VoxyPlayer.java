package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.interfaces.IVoxyClient;
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

	@Override
	public IVoxyClient getClient() {
		return client;
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

		// TODO: Send a request to the server to mute or unmute the player
	}

	@Override
	public boolean isDeaf() {
		return isDeaf;
	}
}
