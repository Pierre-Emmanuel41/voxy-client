package fr.pederobien.voxy.client.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.client.event.VoxyRoomAddedEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRemovedEvent;
import fr.pederobien.voxy.client.impl.RoomList;
import fr.pederobien.voxy.client.interfaces.IRoomList;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class RoomListImpl extends ClientElement {
	private final List<VoxyRoomImpl> rooms;
	private final Object lock;

	private final IRoomList external;

	/**
	 * Creates the implementation of a rooms list.
	 * 
	 * @param client The client implementation associated to this rooms list implementation.
	 */
	protected RoomListImpl(VoxyClientImpl client) {
		super(client);

		rooms = new ArrayList<VoxyRoomImpl>();
		lock = new Object();

		external = new RoomList(this);
	}

	/**
	 * Sends a request to the server to add a room.
	 * 
	 * @param name The name of the room to add.
	 */
	public void sendRoomAddRequest(String name) {
		getClient().getNotifier().sendRoomAddRequest(name);
	}

	/**
	 * Creates a new room implementation and add it to the underlying list.
	 *
	 * @param name The room's name to add.
	 * 
	 * @return The created room.
	 */
	public VoxyRoomImpl add(String name, int port) {
		VoxyRoomImpl roomImpl = new VoxyRoomImpl(getClient(), name, port);
		synchronized (lock) {
			rooms.add(roomImpl);
		}

		info("Room %s has been added", roomImpl);
		EventManager.callEvent(new VoxyRoomAddedEvent(roomImpl.getExternal()));

		return roomImpl;
	}

	/**
	 * Sends a request to the server to remove a room.
	 * 
	 * @param name The name of the room to remove.
	 */
	public void sendRoomRemoveRequest(String name) {
		getClient().getNotifier().sendRoomRemoveRequest(name);
	}

	/**
	 * Removes the room implementation from the rooms list.
	 * 
	 * @param roomImpl The room implementation to remove.
	 */
	public void remove(VoxyRoomImpl roomImpl) {
		synchronized (lock) {
			rooms.remove(roomImpl);
		}

		info("Room %s has been removed", roomImpl.getName());
		EventManager.callEvent(new VoxyRoomRemovedEvent(roomImpl.getExternal()));
	}

	/**
	 * Get the room implementation associated to the given name.
	 * 
	 * @param name The name of the room implementation to get.
	 * 
	 * @return The associated room implementation if found, null otherwise.
	 */
	public VoxyRoomImpl getByName(String name) {
		synchronized (lock) {
			for (VoxyRoomImpl roomImpl : rooms)
				if (roomImpl.getName().equals(name))
					return roomImpl;
		}

		return null;
	}

	/**
	 * @return The list of rooms to use externally.
	 */
	public List<IVoxyRoom> toList() {
		List<IVoxyRoom> list = new ArrayList<IVoxyRoom>();
		synchronized (lock) {
			for (VoxyRoomImpl roomImpl : rooms)
				list.add(roomImpl.getExternal());
		}

		return list;
	}

	/**
	 * Performs the given action over each rooms in this list.
	 * 
	 * @param action The action to perform.
	 */
	public void foreach(Consumer<VoxyRoomImpl> action) {
		synchronized (lock) {
			rooms.forEach(action);
		}
	}

	/**
	 * @return The rooms list to use externally.
	 */
	public IRoomList getExternal() {
		return external;
	}
}
