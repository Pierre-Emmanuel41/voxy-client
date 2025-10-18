package fr.pederobien.voxy.client.interfaces;

import java.util.Map;

public interface IVoxyRoom {

	/**
	 * @return The voxy client associated to this room.
	 */
	IVoxyClient getClient();

	/**
	 * @return The name of the room.
	 */
	String getName();

	/**
	 * Set the name of this room.
	 *
	 * @param name The new room's name.
	 */
	void setName(String name);

	/**
	 * @return The map of player currently connected in this room. This map is unmodifiable.
	 */
	Map<String, IVoxyPlayer> getPlayers();
}
