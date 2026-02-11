package fr.pederobien.voxy.client.interfaces;

public interface IVoxySoundApi {

	/**
	 * Initialize the sound api to have access to the microphone and speakers. An exception is thrown if the API could not be
	 * initialized successfully.
	 */
	void initialize() throws Exception;

	/**
	 * Free all the resources.
	 */
	void dispose();

	/**
	 * @return The microphone used to send audio samples to the server.
	 */
	IVoxyMicrophone getMicrophone();

	/**
	 * @return The speakers used to play audio samples received from the server.
	 */
	IVoxySpeakers getSpeakers();
}
