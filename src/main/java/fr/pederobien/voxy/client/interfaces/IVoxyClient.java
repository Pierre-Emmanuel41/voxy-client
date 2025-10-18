package fr.pederobien.voxy.client.interfaces;

import java.util.Map;
import java.util.function.Consumer;

public interface IVoxyClient {

	/**
	 * Opens the connection with the server. When the connection is established, a VoxyClientConnected event is thrown.
	 */
	void connect();

	/**
	 * Close the connection with the server.
	 */
	void disconnect();

	/**
	 * Dispose this client. It cannot be reused to communicate with the remote.
	 */
	void dispose();

	/**
	 * @return True if the client is disposed, false otherwise..
	 */
	boolean isDisposed();

	/**
	 * @return The map of rooms where player can talk to each other. This map is unmodifiable.
	 */
	Map<String, IVoxyRoom> getRooms();

	/**
	 * Sends a request to the server to add a room.
	 *
	 * @param name     The room's name to add.
	 * @param callback The callback to execute when a response from the server has been received. The boolean represents a success
	 *                 status.
	 */
	void add(String name, Consumer<Boolean> callback);

	/**
	 * Sends a request to the server to remove a room.
	 *
	 * @param name     The room's name to remove.
	 * @param callback The callback to execute when a response from the server has been received. The boolean represents a success
	 *                 status.
	 */
	void remove(String name, Consumer<Boolean> callback);
}
