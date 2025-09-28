package fr.pederobien.voxy.client.impl;

import fr.pederobien.voxy.client.interfaces.IVoxyClient;

public class Voxy {

    /**
     * Creates a client to communicate with a voxy server.
     *
     * @param playerName The player's name.
     * @param address    The server's address.
     * @param port       The server's port number.
     */
    public static final IVoxyClient createClient(String playerName, String address, int port) {
        return new VoxyClient(playerName, address, port);
    }
}
