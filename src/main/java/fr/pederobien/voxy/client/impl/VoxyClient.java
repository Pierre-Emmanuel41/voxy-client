package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.impl.internal.VoxyClientImpl;
import fr.pederobien.voxy.client.interfaces.IRoomList;
import fr.pederobien.voxy.client.interfaces.IVoxyClient;
import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyClient implements IVoxyClient {
	private final VoxyClientImpl impl;

	/**
	 * Creates a voxy client to communicate with a voxy server.
	 *
	 * @param impl The implementation of this voxy client.
	 */
	public VoxyClient(VoxyClientImpl impl) {
		this.impl = impl;
	}

	@Override
	public void connect() {
		impl.connect();
	}

	@Override
	public void disconnect() {
		impl.disconnect();
	}

	@Override
	public void dispose() {
		impl.dispose();
	}

	@Override
	public boolean isDisposed() {
		return impl.isDisposed();
	}

	@Override
	public IRoomList getRooms() {
		return impl.getRooms().getExternal();
	}

	@Override
	public IVoxyMainPlayer getPlayer() {
		return impl.getPlayer().getExternal();
	}

	@Override
	public String toString() {
		return impl.toString();
	}
}
