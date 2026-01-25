package fr.pederobien.voxy.client.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.client.interfaces.IVoxyMainPlayer;

public class VoxyMainPlayerSpeakingEvent extends VoxyMainPlayerEvent {
	private final byte[] sample;
	private final byte algorithm;

	/**
	 * Creates an event thrown when the voxy main player is speaking.
	 * 
	 * @param player    The main player.
	 * @param sample    The bytes array that contains the audio sample.
	 * @param algorithm The algorithm used to compress the audio sample.
	 */
	public VoxyMainPlayerSpeakingEvent(IVoxyMainPlayer player, byte[] sample, byte algorithm) {
		super(player);

		this.sample = sample;
		this.algorithm = algorithm;
	}

	/**
	 * @return The algorithm used to compress the audio sample.
	 */
	public byte getAlgorithm() {
		return algorithm;
	}

	/**
	 * @return The bytes array that contains the audio sample.
	 */
	public byte[] getSample() {
		return sample;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("sampleSize=" + getSample().length);
		joiner.add("algorithm=" + getAlgorithm());
		return String.format("%s_%s", getName(), joiner);
	}
}
