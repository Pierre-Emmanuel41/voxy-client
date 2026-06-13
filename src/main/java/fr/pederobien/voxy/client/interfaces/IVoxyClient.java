package fr.pederobien.voxy.client.interfaces;

import fr.pederobien.sound.interfaces.ISoundApi;

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
	 * @return The list of rooms.
	 */
	IRoomList getRooms();

	/**
	 * @return The player associated to this client.
	 */
	IVoxyMainPlayer getPlayer();

	/**
	 * @return The sound API to use to access microphone and speakers.
	 */
	ISoundApi getSoundApi();
}
