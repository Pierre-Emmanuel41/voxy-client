package fr.pederobien.voxy.client.interfaces;

public interface IVoxyRoom {

	/**
	 * @return The name of the room.
	 */
	String getName();

	/**
	 * Set the name of this room. The room's name is not directly modified. It sends first a request to the server. If the request is
	 * denied, a VoxyRoomRenameFailureEvent is thrown. If the request is allowed, the server will update the room's name and notify
	 * this client, resulting of throwing a VoxyRoomRenamedEvent.
	 *
	 * @param name The new room's name.
	 */
	void setName(String name);

	/**
	 * @return The list of players registered in this room.
	 */
	IPlayerList getPlayers();

	/**
	 * Join this room to speak with other players present in this room. A request is sent to the server and if the request is denied,
	 * a VoxyRoomJoinFailureEvent is thrown. If the request is allowed, the server will add the player to the room and notify this
	 * client. If the client's initialization sequence failed with the room's server, a VoxyRoomJoinFailureEvent is thrown. Otherwise,
	 * a VoxyRoomJoinedEvent is thrown.
	 */
	void join();

	/**
	 * Leave this room. A request is sent to the server and if the request is denied a VoxyRoomLeaveFailureEvent is thrown. If the
	 * request is allowed, the server will remove the player from the room and notify this client, resulting of throwing a
	 * VoxyRoomLeftEvent.
	 */
	void leave();
}
