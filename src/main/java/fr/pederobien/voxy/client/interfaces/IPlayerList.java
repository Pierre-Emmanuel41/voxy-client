package fr.pederobien.voxy.client.interfaces;

import java.util.List;
import java.util.Optional;

public interface IPlayerList {

	/**
	 * Get the player associated to the given name.
	 * 
	 * @param name The name of the player to return.
	 * 
	 * @return An optional containing the player if registered, an empty optional otherwise.
	 */
	Optional<IVoxyPlayer> get(String name);

	/**
	 * @return The number of players in the room.
	 */
	int size();

	/**
	 * @return An unmodifiable list of players.
	 */
	List<IVoxyPlayer> toList();
}
