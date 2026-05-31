package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.connection.ICallback.CallbackArgs;
import fr.pederobien.messenger.event.ProtocolClientUnstableEvent;
import fr.pederobien.messenger.event.ProtocolConnectionLostEvent;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.client.EthernetProtocolClientConfig;
import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.client.IProtocolClient;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.protocol.interfaces.IRequest;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.event.VoxyMainPlayerSpeakingEvent;
import fr.pederobien.voxy.client.event.VoxyRoomJoinFailureEvent;
import fr.pederobien.voxy.client.event.VoxyRoomJoinedEvent;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.common.impl.requests.PlayerAudioStreamContentRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerAudioStreamVolumesRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerAudioStreamVolumesRequest.VolumeInfo;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;

public class VocalClient implements IEventListener {
	private final VoxyMainPlayerImpl player;
	private EthernetProtocolClientConfig config;
	private IProtocolClient client;
	private VoxyRoomImpl room;
	private SoundApiManager soundManager;

	/**
	 * Creates a UDP client associated to a player.
	 * 
	 * @param playerName The name of the player that communicate with other player in a room.
	 */
	protected VocalClient(VoxyMainPlayerImpl player) {
		this.player = player;
		soundManager = new SoundApiManager(player.getClient());
		EventManager.registerListener(this);
	}

	/**
	 * Connect this vocal client to the room's vocal server.
	 * 
	 * @param room The room to join.
	 */
	public void connect(VoxyRoomImpl room) {
		if (isConnected())
			disconnect();

		this.room = room;

		// Connecting to the room's vocal server
		IEthernetEndPoint endPoint = new EthernetEndPoint(player.getClient().getAddress(), room.getPort());
		config = Messenger.createEthernetProtocolClientConfig(VoxyProtocolManager.instance(), player.getName() + " - VocalClient", endPoint);
		config.setAutomaticReconnection(false);
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(player.getClient().getCertificate()));

		// Registering event handler
		config.addRequestHandler(VoxyIdentifiers.PLAYER_PROPERTIES, this::onPlayerProperties);
		config.addRequestHandler(VoxyIdentifiers.PLAYER_AUDIO_STREAM_CONTENT, this::onPlayerSpeak);
		config.addRequestHandler(VoxyIdentifiers.PLAYER_AUDIO_STREAM_VOLUMES, this::onAudioVolumesChanged);

		client = Messenger.createUdpProtocolClient(config);

		debug("Connecting player %s to %s's vocal server", player.getName(), room.getName());
		client.connect();
	}

	/**
	 * Disconnect the vocal client from the room's server.
	 */
	public void disconnect() {
		// Vocal Client not connected to a room's server
		if (!isConnected())
			return;

		room = null;
		setMute(true);
		setDeaf(true);
		soundManager.resetVolumes();
		client.disconnect();
	}

	/**
	 * Dispose the vocal client, free all resources to communicate with the server and all resources used by the sound API.
	 */
	public void dispose() {
		soundManager.dispose();

		// Client disposed even if not connected to a room's vocal server
		if (client != null)
			client.dispose();
	}

	/**
	 * Set if the vocal client shall enable or disable the player's microphone.
	 * 
	 * @param isMute True to disable player's microphone, false to enable it.
	 */
	public void setMute(boolean isMute) {
		soundManager.setMute(isMute);
	}

	/**
	 * Set if the vocal client shall enable or disable the player's speakers.
	 * 
	 * @param isDeaf True to disable player's speakers, false to enable them.
	 */
	public void setDeaf(boolean isDeaf) {
		soundManager.setDeaf(isDeaf);
	}

	/**
	 * @return True if this client client is connected to a room's vocal server, false otherwise.
	 */
	public boolean isConnected() {
		return room != null;
	}

	@EventHandler
	private void onPlayerJoinedRoom(VoxyRoomJoinedEvent event) {
		if (!event.getPlayer().getName().equals(player.getName()))
			return;

		soundManager.setMute(player.isMute());
		soundManager.setDeaf(player.isDeaf());
	}

	@EventHandler
	private void onMainPlayerSpeaking(VoxyMainPlayerSpeakingEvent event) {
		if (event.getPlayer() != player.getExternal())
			return;

		send(VoxyIdentifiers.PLAYER_AUDIO_STREAM_CONTENT, new PlayerAudioStreamContentRequest(player.getName(), event.getSample(), event.getAlgorithm()));
	}

	@EventHandler
	private void onConnectionLost(ProtocolConnectionLostEvent event) {
		if (client == null || event.getConnection() != client.getConnection())
			return;

		disconnect();
	}

	@EventHandler
	private void onUnstableClientEvent(ProtocolClientUnstableEvent event) {
		if (client == null || event.getClient() != client)
			return;

		disconnect();
	}

	/**
	 * Event handler: Method called when the server requires player's properties.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param ignored    The payload sent by the server to get player properties.
	 */
	private void onPlayerProperties(IProtocolConnection connection, int messageID, Object ignored) {
		debug("Server requires player's properties");
		PlayerPropertiesRequest payload = new PlayerPropertiesRequest(player.getName(), player.isMute(), player.isDeaf());

		debug("Sending following payload: %s", payload);
		IRequestMessage request = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, payload);
		request.setCallback(args -> handlePlayerPropertiesResponse(args));
		answer(messageID, request);
	}

	/**
	 * Event handler: Method called when the server notify this client that a player is speaking
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The payload that contains the name of the speaking player and the audio content.
	 */
	private void onPlayerSpeak(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerAudioStreamContentRequest request))
			return;

		soundManager.write(request.getName(), request.getSample(), request.getAlgorithm());
	}

	/**
	 * Event handler: Method called when the server notify this client that the volumes level for a player has changed.
	 *
	 * @param connection The connection with the server.
	 * @param messageID  The server's message identifier.
	 * @param payload    The payload that contains the name of the speaking player and the new audio volumes.
	 */
	private void onAudioVolumesChanged(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerAudioStreamVolumesRequest request))
			return;

		for (VolumeInfo info : request.getVolumes()) {
			String format = "%s's audio volumes changed: left=%s, right=%s, global=%s";
			debug(format, player.getName(), info.getName(), info.getLeft(), info.getRight(), info.getGlobal());
			soundManager.setVolumes(info.getName(), info.getLeft(), info.getRight(), info.getGlobal());
		}
	}

	/**
	 * Method called when the server accepts or not player's properties.
	 *
	 * @param argument The argument that contains server's response.
	 */
	private void handlePlayerPropertiesResponse(CallbackArgs argument) {
		if (argument.isTimeout() || argument.isConnectionLost()) {
			debug("A timeout or a connection lost happened after the client sent player's properties");
			EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
			room = null;
			return;
		}

		IRequest response = config.parse(argument.response());

		if (response == null) {
			Logger.error("Technical error happened: Could not parse server's response for player's properties");
			EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
			room = null;
			return;
		}

		if (response.getIdentifier() != VoxyIdentifiers.ACKNOWLEDGEMENT) {
			debug("The server did not acknowledge back player's properties");
			EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
			room = null;
			return;
		}

		if (response.getError() != VoxyErrors.NO_ERROR) {
			debug("The server did not accept player properties: %s", response.getError().getMessage());
			EventManager.callEvent(new VoxyRoomJoinFailureEvent(room.getName()));
			room = null;
			return;
		}

		info("Connected successfully to %s's vocal server", room.getName());
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
	 * @param messageID The identifier of the message received from the remote.
	 * @param request   The request to send to the remote.
	 */
	private void answer(int messageID, IRequestMessage request) {
		client.getConnection().answer(messageID, request);
	}

	/**
	 * Creates a request based on the given identifier and payload and send it to the server.
	 * 
	 * @param identifier The request's identifier.
	 * @param payload    The request's payload.
	 */
	private void send(IIdentifier identifier, Object payload) {
		client.getConnection().send(getRequest(identifier, VoxyErrors.NO_ERROR, payload));
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void info(String message, Object... args) {
		Logger.info("%s - %s", client, String.format(message, args));
	}

	/**
	 * Print a log using DEBUG level.
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void debug(String format, Object... args) {
		Logger.debug(3, "%s - %s", client, String.format(format, args));
	}

	/**
	 * Print a log using ERROR level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void error(String format, Object... args) {
		Logger.error("%s %s", client, String.format(format, args));
	}
}
