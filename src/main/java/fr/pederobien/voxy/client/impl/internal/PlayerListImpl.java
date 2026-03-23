package fr.pederobien.voxy.client.impl.internal;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.client.event.VoxyRoomJoinedEvent;
import fr.pederobien.voxy.client.event.VoxyRoomLeftEvent;
import fr.pederobien.voxy.client.impl.PlayerList;
import fr.pederobien.voxy.client.interfaces.IPlayerList;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class PlayerListImpl extends ClientElement {
	private final VoxyRoomImpl roomImpl;
	private final List<VoxyPlayerImpl> players;
	private final Object lock;

	private final IPlayerList external;

	/**
	 * Creates the implementation of a players list.
	 * 
	 * @param roomImpl The room implementation associated to this players list implementation.
	 */
	protected PlayerListImpl(VoxyRoomImpl roomImpl) {
		super(roomImpl.getClient());

		this.roomImpl = roomImpl;

		players = new ArrayList<VoxyPlayerImpl>();
		lock = new Object();

		external = new PlayerList(this);
	}

	/**
	 * Adds the given player to the underlying list of players.
	 * 
	 * @param player The player to add.
	 */
	public void add(VoxyPlayerImpl playerImpl) {
		synchronized (lock) {
			players.add(playerImpl);
		}

		info("Player %s joined the room %s", playerImpl, roomImpl);
		EventManager.callEvent(new VoxyRoomJoinedEvent(roomImpl.getExternal(), playerImpl.getExternal()));
	}

	public void remove(VoxyPlayerImpl playerImpl) {
		synchronized (lock) {
			players.remove(playerImpl);
		}

		info("Player %s left the room %s", playerImpl, roomImpl);
		EventManager.callEvent(new VoxyRoomLeftEvent(roomImpl.getExternal(), playerImpl.getExternal()));
	}

	/**
	 * Get the player implementation associated to the given name.
	 * 
	 * @param name The name associated to the player implementation.
	 * 
	 * @return The player implementation if found, null otherwise.
	 */
	public VoxyPlayerImpl getByName(String name) {
		synchronized (lock) {
			for (VoxyPlayerImpl player : players)
				if (player.getName().equals(name))
					return player;
		}

		return null;
	}

	/**
	 * @return The number of players in the underlying list.
	 */
	public int size() {
		return players.size();
	}

	/**
	 * @return The list of players to use externally.
	 */
	public List<IVoxyPlayer> toList() {
		List<IVoxyPlayer> list = new ArrayList<IVoxyPlayer>();
		synchronized (lock) {
			for (VoxyPlayerImpl playerImpl : players)
				list.add(playerImpl.getExternal());
		}

		return list;
	}

	/**
	 * Removes all players registered in this list.
	 */
	public void clear() {
		List<VoxyPlayerImpl> copy = new ArrayList<VoxyPlayerImpl>(players);

		synchronized (lock) {
			players.clear();
		}

		for (VoxyPlayerImpl player : copy)
			EventManager.callEvent(new VoxyRoomLeftEvent(roomImpl.getExternal(), player.getExternal()));
	}

	/**
	 * @return The players list to use externally.
	 */
	public IPlayerList getExternal() {
		return external;
	}
}
