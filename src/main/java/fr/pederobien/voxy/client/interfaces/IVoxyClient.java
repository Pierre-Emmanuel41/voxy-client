package fr.pederobien.voxy.client.interfaces;

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
	 * @return The list of rooms.
	 */
	IRoomList getRooms();
}
