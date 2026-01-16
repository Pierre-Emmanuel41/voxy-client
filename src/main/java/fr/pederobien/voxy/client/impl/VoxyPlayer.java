package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class VoxyPlayer implements IVoxyPlayer {
	private VoxyPlayerImpl impl;

	/**
	 * Creates a voxy player.
	 * 
	 * @param impl The implementation associated to this voxy player.
	 */
	public VoxyPlayer(VoxyPlayerImpl impl) {
		this.impl = impl;
	}

	@Override
	public String getName() {
		return impl.getName();
	}

	@Override
	public boolean isMute() {
		return impl.isMute();
	}

	@Override
	public void setMute(boolean isMute) {
		impl.sendPlayerMuteStatusChanged(isMute);
	}

	@Override
	public boolean isMuteByMainPlayer() {
		return impl.isMuteByMainPlayer();
	}

	@Override
	public boolean isDeaf() {
		return impl.isDeaf();
	}

	@Override
	public String toString() {
		return impl.toString();
	}
}
