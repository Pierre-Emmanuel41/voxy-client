package fr.pederobien.voxy.client.impl.internal;

import fr.pederobien.voxy.client.interfaces.IAudioSample;

public class AudioSample implements IAudioSample {
	private final String name;
	private final byte[] data;
	private final float left;
	private final float right;
	private final float global;

	/**
	 * Creates an audio sample to play.
	 * 
	 * @param name   The player's name associated to this audio sample.
	 * @param data   The bytes array that contains the audio sample.
	 * @param left   The volume on the left side.
	 * @param right  The volume on the right side.
	 * @param global The global volume on both sides.
	 */
	public AudioSample(String name, byte[] data, float left, float right, float global) {
		this.name = name;
		this.data = data;
		this.left = left;
		this.right = right;
		this.global = global;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public float getLeft() {
		return left;
	}

	@Override
	public float getRight() {
		return right;
	}

	@Override
	public float getGlobal() {
		return global;
	}
}
