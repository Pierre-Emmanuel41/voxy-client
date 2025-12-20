package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.utils.event.Event;

public class VoxyMicrophoneOpenFailureEvent extends Event {
	private final Exception exception;

	/**
	 * Creates an event thrown when an error occurred while opening the microphone.
	 * 
	 * @param exception The exception thrown by the microphone.
	 */
	public VoxyMicrophoneOpenFailureEvent(Exception exception) {
		this.exception = exception;
	}

	/**
	 * @return The exception thrown by the microphone.
	 */
	public Exception getException() {
		return exception;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("message=" + exception.getMessage());
		return String.format("%s_%s", getName(), joiner.toString());
	}
}
