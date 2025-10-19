package fr.pederobien.voxy.client.impl;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.client.ProtocolClientConfig;
import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.protocol.interfaces.IRequest;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyRoomJoinFailureEvent;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;

public class VocalClient implements IEventListener {
	private final VoxyClient voxyClient;
	private ProtocolClientConfig<IEthernetEndPoint> config;
	private IProtocolClient vocalClient;
	private VoxyRoom room;

	/**
	 * Creates a UDP client associated to a player.
	 * 
	 * @param playerName The name of the player that communicate with other player in a room.
	 */
	public VocalClient(VoxyClient client) {
		this.voxyClient = client;

		EventManager.registerListener(this);
	}

	/**
	 * Connect this vocal client to the room's vocal server.
	 * 
	 * @param room The room to join.
	 */
	public void connect(VoxyRoom room) {
		this.room = room;

		// Connecting to the room's vocal server
		IEthernetEndPoint endPoint = new EthernetEndPoint(room.getAddress(), room.getPort());
		config = Messenger.createClientConfig(VoxyProtocolManager.instance(), voxyClient.getPlayerName() + " - VocalClient", endPoint);

		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		// Registering event handler
		config.addRequestHandler(VoxyIdentifiers.PLAYER_PROPERTIES, this::onPlayerProperties);

		vocalClient = Messenger.createUdpClient(config);

		debug("Connecting player %s to %s's vocal server", voxyClient.getPlayerName(), room.getName());
		vocalClient.connect();
	}

	/**
	 * Disconnect the vocal client from the room's server.
	 */
	public void disconnect() {
		debug("Disconnecting vocal client from vocal server");

		vocalClient.disconnect();
		room.remove(voxyClient.getPlayerName());
	}

	/**
	 * Event handler: Method called when the server notify the client that a room has been added.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 */
	private void onPlayerProperties(IProtocolConnection connection, int messageID, Object ignored) {
		debug("Server requires player's properties");
		PlayerPropertiesRequest payload = new PlayerPropertiesRequest(voxyClient.getPlayerName(), voxyClient.isMute(), voxyClient.isDeaf());

		debug("Sending following payload: %s", payload);
		IRequestMessage request = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, payload);
		request.setCallback(args -> handlePlayerPropertiesResponse(args));
		answer(messageID, request);
	}

	/**
	 * Method called when the server accepts or not player's properties.
	 *
	 * @param argument The argument that contains server's response.
	 */
	private void handlePlayerPropertiesResponse(CallbackArgs argument) {
		if (!(argument.isTimeout() || argument.isConnectionLost())) {
			IRequest response = config.parse(argument.response());

			if (response == null) {
				Logger.error("Technical error happened: Could not parse server's response for player's properties");
				EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
				return;
			}

			if (response.getIdentifier() != VoxyIdentifiers.ACKOWLEDGEMENT) {
				debug("The server did not acknowledge back player's properties");
				EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
				return;
			}

			if (response.getError() != VoxyErrors.NO_ERROR) {
				debug("The server did not accept player properties: %s", response.getError().getMessage());
				EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
				return;
			}

			info("Connected successfully to %s's vocal server", room.getName());
			room.add(new VoxyPlayer(voxyClient, voxyClient.getPlayerName(), voxyClient.isMute(), voxyClient.isDeaf()));
		} else {
			debug("A timeout or a connection lost happened after the client sent player's properties");
			EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
		}
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
	private IRequestMessage getRequest(IIdentifier identifier, IError error, Object payload) {
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
	private IRequestMessage getRequest(IIdentifier identifier, Object payload) {
		return getRequest(identifier, VoxyErrors.NO_ERROR, payload);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param request The request to send to the remote.
	 */
	private void send(IRequestMessage request) {
		vocalClient.getConnection().send(request);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param messageID The identifier of the message received from the remote.
	 * @param request   The request to send to the remote.
	 */
	private void answer(int messageID, IRequestMessage request) {
		vocalClient.getConnection().answer(messageID, request);
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void info(String message, Object... args) {
		Logger.info("%s - %s", vocalClient, String.format(message, args));
	}

	/**
	 * Print a log using DEBUG level.
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void debug(String format, Object... args) {
		Logger.debug("%s - %s", vocalClient, String.format(format, args));
	}
}
