package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.impl.internal.VoxyRoomImpl;
import fr.pederobien.voxy.client.interfaces.IPlayerList;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class VoxyRoom implements IVoxyRoom {
	private VoxyRoomImpl impl;

	/**
	 * Creates a room that a player can join to speak with other player in this room.
	 * 
	 * @param impl The implementation of this room.
	 */
	public VoxyRoom(VoxyRoomImpl impl) {
		this.impl = impl;
	}

	@Override
	public String getName() {
		return impl.getName();
	}

	@Override
	public void setName(String name) {
		impl.sendRoomRenameRequest(name);
	}

	@Override
	public IPlayerList getPlayers() {
		return impl.getPlayers().getExternal();
	}

	@Override
	public void join() {
		impl.sendRoomJoinRequest();
	}

	@Override
	public void leave() {
		impl.sendRoomLeaveRequest();
	}
}
