package fr.pederobien.voxy.client.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.voxy.client.impl.internal.PlayerListImpl;
import fr.pederobien.voxy.client.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.client.interfaces.IPlayerList;
import fr.pederobien.voxy.client.interfaces.IVoxyPlayer;

public class PlayerList implements IPlayerList {
	private final PlayerListImpl impl;

	/**
	 * Creates a list of players.
	 * 
	 * @param impl The implementation of this list of players.
	 */
	public PlayerList(PlayerListImpl impl) {
		this.impl = impl;
	}

	@Override
	public Optional<IVoxyPlayer> get(String name) {
		VoxyPlayerImpl playerImpl = impl.getByName(name);
		return playerImpl == null ? Optional.empty() : Optional.of(playerImpl.getExternal());
	}

	@Override
	public List<IVoxyPlayer> toList() {
		return impl.toList();
	}
}
