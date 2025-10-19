package fr.pederobien.voxy.client.interfaces;

import java.util.Map;

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
	 * @return The name of the player associated to this client.
	 */
	String getPlayerName();

	/**
	 * @return True if the player is mute, false otherwise.
	 */
	boolean isMute();

	/**
	 * @return True if the player is deaf, false otherwise.
	 */
	boolean isDeaf();

	/**
	 * @return The map of rooms where player can talk to each other. This map is unmodifiable.
	 */
	Map<String, IVoxyRoom> getRooms();

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
}
