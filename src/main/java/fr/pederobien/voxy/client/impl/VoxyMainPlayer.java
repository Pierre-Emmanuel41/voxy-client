package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.impl.internal.VoxyMainPlayerImpl;
import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyMainPlayer implements IVoxyMainPlayer {
	private final VoxyMainPlayerImpl impl;

	/**
	 * Creates the main player of the voxy application.
	 * 
	 * @param impl The main player implementation.
	 */
	public VoxyMainPlayer(VoxyMainPlayerImpl impl) {
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
	public boolean isDeaf() {
		return impl.isDeaf();
	}

	@Override
	public void setDeaf(boolean isDeaf) {
		impl.sendPlayerDeafStatusChanged(isDeaf);
	}
}
