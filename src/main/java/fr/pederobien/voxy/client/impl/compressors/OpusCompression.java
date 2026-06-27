package fr.pederobien.voxy.client.impl.compressors;

import java.nio.ByteOrder;

import de.maxhenkel.opus4j.OpusDecoder;
import de.maxhenkel.opus4j.OpusEncoder;
import de.maxhenkel.opus4j.OpusEncoder.Application;
import fr.pederobien.utils.ByteWrapper;
import fr.pederobien.utils.ReadableByteWrapper;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.client.interfaces.ISampleCompressor;

public class OpusCompression implements ISampleCompressor {
	private static final int FRAME_SIZE_MS = 20;
	private final ByteOrder byteOrder;
	private final OpusEncoder encoder;
	private final OpusDecoder decoder;
	private final int frameSize;
	private final int bytesPerFrame;

	/**
	 * Creates a Voice Over IP compressor.
	 * 
	 * @param sampleRate The audio sample rate.
	 */
	public OpusCompression(float sampleRate, boolean bigEndian) {
		this.byteOrder = bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

		frameSize = (int) Math.round(sampleRate * FRAME_SIZE_MS / 1000);
		bytesPerFrame = frameSize * 2;
		try {
			encoder = new OpusEncoder((int) sampleRate, 1, Application.VOIP);
			decoder = new OpusDecoder((int) sampleRate, 1);
			decoder.setFrameSize(frameSize);
		} catch (Exception e) {
			Logger.error("The following error occurred while creating Opus compressor: %s", e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getBytesPerFrame() {
		return bytesPerFrame;
	}

	@Override
	public int getAlgorithm() {
		return 2;
	}

	@Override
	public byte[] compress(byte[] buffer) throws Exception {
		// Step 1: Checking bytes array size against frameSize
		if (buffer.length != bytesPerFrame) {
			String format = "The buffer to compress has incorrect size, expecting %s but got %s";
			throw new IllegalArgumentException(String.format(format, bytesPerFrame, buffer.length));
		}

		ReadableByteWrapper wrapper = ReadableByteWrapper.wrap(buffer, byteOrder);
		short[] shorts = new short[frameSize];

		for (int i = 0; i < frameSize; i++)
			shorts[i] = wrapper.nextShort();

		return encoder.encode(shorts);
	}

	@Override
	public byte[] decompress(byte[] buffer) throws Exception {
		short[] shorts = decoder.decode(buffer);
		ByteWrapper wrapper = ByteWrapper.create(byteOrder);

		for (short s : shorts)
			wrapper.putShort(s);

		return wrapper.get();
	}
}
