package fr.pederobien.voxy.client.interfaces;

import java.util.List;
import java.util.Optional;

public interface IRoomList {

	/**
	 * Sends a request to the server to add a room. If the request is denied, a VoxyRoomAddFailureEvent is thrown. If the request is
	 * allowed, the server will create a room and notify this client, resulting of throwing a VoxyRoomAddedEvent.
	 *
	 * @param name The room's name to add.
	 */
	void add(String name);

	/**
	 * Sends a request to the server to remove a room. If the request is denied, a VoxyRoomRemoveFailureEvent is thrown. If the
	 * request is allowed, the server will remove the room and notify this client, resulting of throwing a VoxyRoomRemovedEvent.
	 *
	 * @param name The room's name to remove.
	 */
	void remove(String name);

	/**
	 * Finds the room associated to the given name.
	 * 
	 * @param name The room's name to find.
	 * 
	 * @return An optional containing the room with the given name if it exists, an empty optional otherwise.
	 */
	Optional<IVoxyRoom> get(String name);

	/**
	 * Get the room in which the player associated to the given name is.
	 * 
	 * @param name The name of the player.
	 * 
	 * @return An optional containing the room in which the player is, an empty optional if the player is not registered in a room.
	 */
	Optional<IVoxyRoom> getRoomByPlayerName(String name);

	/**
	 * @return The number of rooms in the underlying list.
	 */
	int size();

	/**
	 * @return An unmodifiable list containing the rooms registered in this list.
	 */
	List<IVoxyRoom> toList();
}
