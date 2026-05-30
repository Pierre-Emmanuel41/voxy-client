package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.utils.event.Logger;

public class ClientElement {
	private final VoxyClientImpl client;

	/**
	 * Creates an element associated to a client.
	 * 
	 * @param client The client implementation associated to this element.
	 */
	protected ClientElement(VoxyClientImpl client) {
		this.client = client;
	}

	/**
	 * @return The client associated to this element.
	 */
	protected VoxyClientImpl getClient() {
		return client;
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String format, Object... args) {
		Logger.debug(3, "%s %s", getClient(), String.format(format, args));
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String format, Object... args) {
		Logger.info("%s %s", getClient(), String.format(format, args));
	}

	/**
	 * Print a log using ERROR level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void error(String format, Object... args) {
		Logger.error("%s %s", getClient(), String.format(format, args));
	}
}
