package fr.pederobien.voxy.client.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.voxy.client.impl.internal.RoomListImpl;
import fr.pederobien.voxy.client.impl.internal.VoxyRoomImpl;
import fr.pederobien.voxy.client.interfaces.IRoomList;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class RoomList implements IRoomList {
	private final RoomListImpl impl;

	/**
	 * Creates a list of rooms.
	 * 
	 * @param impl The implementation of this rooms list.
	 */
	public RoomList(RoomListImpl impl) {
		this.impl = impl;
	}

	@Override
	public void add(String name, int port) {
		impl.sendRoomAddRequest(name, port);
	}

	@Override
	public void remove(String name) {
		impl.sendRoomRemoveRequest(name);
	}

	@Override
	public Optional<IVoxyRoom> get(String name) {
		VoxyRoomImpl roomImpl = impl.getByName(name);
		return roomImpl == null ? Optional.empty() : Optional.of(roomImpl.getExternal());
	}

	@Override
	public Optional<IVoxyRoom> getRoomByPlayerName(String name) {
		VoxyRoomImpl room = impl.getRoomByPlayerName(name);
		return Optional.ofNullable(room == null ? null : room.getExternal());
	}

	@Override
	public int size() {
		return impl.size();
	}

	@Override
	public List<IVoxyRoom> toList() {
		return impl.toList();
	}
}
