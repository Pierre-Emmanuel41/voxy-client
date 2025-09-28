package fr.pederobien.voxy.client.interfaces;

import java.util.function.Consumer;

public interface IVoxyClient {

    /**
     * Opens the connection with the server.
     *
     * @param callback The callback to execute. The input parameters is true when the client is connected,
     *                 false when the connection failed.
     */
    void connect(Consumer<Boolean> callback);

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
}
