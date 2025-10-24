package fr.pederobien.voxy.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyRoomAddedEvent;
import fr.pederobien.voxy.client.event.VoxyRoomRemovedEvent;
import fr.pederobien.voxy.client.interfaces.IRoomList;
import fr.pederobien.voxy.client.interfaces.IVoxyRoom;

public class RoomList implements IRoomList {
	private final VoxyClient client;
	private final List<VoxyRoom> rooms;
	private final Object lock;

	/**
	 * Creates a rooms list.
	 * 
	 * @param client The client to use to send request to the server.
	 */
	public RoomList(VoxyClient client) {
		this.client = client;

		rooms = new ArrayList<VoxyRoom>();
		lock = new Object();
	}

	@Override
	public void add(String name) {
		client.sendRoomAddRequest(name);
	}

	@Override
	public void remove(String name) {
		client.sendRoomRemoveRequest(name);
	}

	@Override
	public Optional<IVoxyRoom> get(String name) {
		return Optional.ofNullable(getByName(name));
	}

	@Override
	public int size() {
		return rooms.size();
	}

	@Override
	public List<IVoxyRoom> toList() {
		return Collections.unmodifiableList(rooms);
	}

	/**
	 * Adds the given room to the underlying rooms list.
	 * 
	 * @param room The room to add.
	 */
	protected void add(VoxyRoom room) {
		synchronized (lock) {
			rooms.add(room);
		}

		info("Room %s has been added", room.getName());
		EventManager.callEvent(new VoxyRoomAddedEvent(room));
	}

	/**
	 * Removes the given room from the underlying rooms list.
	 * 
	 * @param room The room to remove.
	 */
	protected void remove(VoxyRoom room) {
		synchronized (lock) {
			rooms.remove(room);
		}

		info("Room %s has been removed", room.getName());
		EventManager.callEvent(new VoxyRoomRemovedEvent(room));
	}

	/**
	 * Get the room associated to the given name.
	 * 
	 * @param name The name of the room to get.
	 * 
	 * @return Null if no room is registered for the given name, the room otherwise.
	 */
	protected VoxyRoom getByName(String name) {
		synchronized (lock) {
			for (VoxyRoom room : rooms)
				if (room.getName().equals(name))
					return room;
		}

		return null;
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void info(String message, Object... args) {
		Logger.info("%s - %s", client, String.format(message, args));
	}
}
