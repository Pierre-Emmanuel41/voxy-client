package fr.pederobien.voxy.client.interfaces;

import java.util.Map;
import java.util.function.Consumer;

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
	 * @param name     The new room's name.
	 * @param callback The action to execute when a response from the server has been received. The boolean represents a success
	 *                 status.
	 */
	void setName(String name, Consumer<Boolean> callback);

	/**
	 * @return The map of player currently connected in this room. This map is unmodifiable.
	 */
	Map<String, IVoxyPlayer> getPlayers();
}
