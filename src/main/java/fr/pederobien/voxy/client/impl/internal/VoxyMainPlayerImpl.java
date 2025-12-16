package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.client.event.VoxyMainPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.client.event.VoxyMainPlayerMuteStatusChangedEvent;
import fr.pederobien.voxy.client.event.VoxyRoomJoinedEvent;
import fr.pederobien.voxy.client.impl.VoxyMainPlayer;
import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyMainPlayerImpl extends ClientElement implements IEventListener {
	private final String name;
	private final VocalClient vocalClient;
	private boolean isMute;
	private boolean isDeaf;

	private final IVoxyMainPlayer external;

	/**
	 * Creates the implementation of a the main player of the voxy application.
	 * 
	 * @param client The client implementation associated to this main player
	 *               implementation.
	 */
	protected VoxyMainPlayerImpl(VoxyClientImpl client, String name) {
		super(client);

		this.name = name;
		isMute = true;
		isDeaf = false;

		vocalClient = new VocalClient(this);
		external = new VoxyMainPlayer(this);

		EventManager.registerListener(this);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return The player's name.
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
	public void sendPlayerMuteStatusChanged(boolean isMute) {
		if (this.isMute == isMute || !vocalClient.isconnected())
			return;

		getClient().getNotifier().sendPlayerMuteStatusChanged(name, isMute);
	}

	/**
	 * @return True if the player disabled it speakers, false otherwise.
	 */
	public boolean isDeaf() {
		return isDeaf;
	}

	/**
	 * Sends a request to the server that player's deaf status has changed.
	 * 
	 * @param isDeaf True if the player is deaf, false if the player is undeaf.
	 */
	public void sendPlayerDeafStatusChanged(boolean isDeaf) {
		if (this.isDeaf == isDeaf || !vocalClient.isconnected())
			return;

		// Updating internal deaf status
		setDeaf(isDeaf);

		getClient().getNotifier().sendPlayerDeafStatusChanged(name, isDeaf);
	}

	/**
	 * @return The vocal client associated to this voxy main player.
	 */
	public VocalClient getVocalClient() {
		return vocalClient;
	}

	/**
	 * @return the voxy main player to use externally.
	 */
	public IVoxyMainPlayer getExternal() {
		return external;
	}

	/**
	 * Update the mute status of this voxy main player.
	 * 
	 * @param isMute True if the player is mute, false otherwise.
	 */
	public void setMute(boolean isMute) {
		// Updating player's mute status
		this.isMute = isMute;

		// Updating vocal client's mute status
		vocalClient.setMute(isMute);

		info("Player %s %s itself", this, isMute ? "muted" : "unmuted");
		EventManager.callEvent(new VoxyMainPlayerMuteStatusChangedEvent(external, isMute));
	}

	@EventHandler
	private void onPlayerJoinedRoom(VoxyRoomJoinedEvent event) {
		if (!event.getPlayer().getName().equals(name))
			return;

		vocalClient.connect(getClient().getRooms().getByName(event.getRoom().getName()));
	}

	/**
	 * Update the deaf status of this voxy main player.
	 * 
	 * @param isDeaf True if the player is deaf, false otherwise.
	 */
	private void setDeaf(boolean isDeaf) {
		// Updating player's deaf status
		this.isDeaf = isDeaf;

		// Updating vocal client's deaf status
		vocalClient.setDeaf(isDeaf);

		info("Player %s %s itself", this, isDeaf ? "deaf" : "undeaf");
		EventManager.callEvent(new VoxyMainPlayerDeafStatusChangedEvent(external, isDeaf));
	}
}
