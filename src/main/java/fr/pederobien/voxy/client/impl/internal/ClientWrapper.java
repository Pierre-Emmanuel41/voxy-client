package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.messenger.interfaces.client.IProtocolClientConfig;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyErrors;

public class ClientWrapper {
	private final IProtocolClient client;
	private final IProtocolClientConfig<IEthernetEndPoint> config;

	/**
	 * Creates a wrapper for the given client.
	 * 
	 * @param client The client to wrap.
	 * @param config The configuration to wrap.
	 */
	public ClientWrapper(IProtocolClient client, IProtocolClientConfig<IEthernetEndPoint> config) {
		this.client = client;
		this.config = config;
	}

	/**
	 * @return The wrapped client.
	 */
	protected IProtocolClient getClient() {
		return client;
	}

	/**
	 * @return The wrapped configuration.
	 */
	public IProtocolClientConfig<IEthernetEndPoint> getConfig() {
		return config;
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param error      The request error.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	protected IRequestMessage getRequest(IIdentifier identifier, IError error, Object payload) {
		return config.getRequest(identifier, error, payload);
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	protected IRequestMessage getRequest(IIdentifier identifier, Object payload) {
		return getRequest(identifier, VoxyErrors.NO_ERROR, payload);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param request The request to send to the remote.
	 */
	protected void send(IRequestMessage request) {
		client.getConnection().send(request);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param messageID The identifier of the message received from the remote.
	 * @param request   The request to send to the remote.
	 */
	protected void answer(int messageID, IRequestMessage request) {
		client.getConnection().answer(messageID, request);
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String message, Object... args) {
		Logger.info("%s - %s", client, String.format(message, args));
	}

	/**
	 * Print a log using WARNING level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void warning(String message, Object... args) {
		Logger.warning("%s - %s", client, String.format(message, args));
	}

	/**
	 * Print a log using DEBUG level.
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String format, Object... args) {
		Logger.debug("%s - %s", client, String.format(format, args));
	}
}
