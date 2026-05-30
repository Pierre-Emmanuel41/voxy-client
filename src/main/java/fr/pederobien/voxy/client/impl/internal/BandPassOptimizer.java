package fr.pederobien.voxy.client.impl.internal;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import fr.pederobien.utils.event.Logger;

public class BandPassOptimizer {
	/**
	 * No compression algorithm should be applied.
	 */
	public static final byte NO_COMPRESSION = 0;

	/**
	 * Use ZLIB to compress the bytes array.
	 */
	public static final byte GZIP = 1;

	/**
	 * Compress the given bytes array using a specific algorithm.
	 * 
	 * @param data      The bytes array to compress.
	 * @param algorithm The algorithm to compress.
	 * @return The compressed byte array, or null if an exception occurred.
	 */
	public static byte[] compress(byte[] data, byte algorithm) {
		switch (algorithm) {
			case GZIP:
				return compressWithGZip(data);
			default:
				return data;
		}
	}

	/**
	 * Uncompress the given bytes array using a specific algorithm.
	 * 
	 * @param data      The bytes array to uncompress.
	 * @param algorithm The algorithm to uncompress.
	 * @return The uncompressed byte array.
	 */
	public static byte[] uncompress(byte[] data, byte algorithm) {
		switch (algorithm) {
			case GZIP:
				return uncompressWithGZip(data);
			default:
				return data;
		}
	}

	/**
	 * Compress the input bytes array using a deflater with default compression level.
	 * 
	 * @param data The bytes array to compress.
	 * @return The compressed bytes array, or null if an exception occurred.
	 */
	private static byte[] compressWithGZip(byte[] data) {
		try {
			Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
			deflater.setInput(data);
			deflater.finish();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
			byte[] buffer = new byte[1024];

			while (!deflater.finished()) {
				int count = deflater.deflate(buffer);
				outputStream.write(buffer, 0, count);
			}

			outputStream.close();
			deflater.end();

			return outputStream.toByteArray();
		} catch (Exception e) {
			Logger.error("An exception occurred while compressing with GZip: %s", e.getMessage());
			return null;
		}
	}

	/**
	 * Compress the input bytes array using a deflater with default compression level.
	 * 
	 * @param data The bytes array to compress.
	 * @return The compressed bytes array, or null if an exception occurred.
	 */
	private static byte[] uncompressWithGZip(byte[] data) {
		try {
			Inflater inflater = new Inflater();
			inflater.setInput(data);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
			byte[] buffer = new byte[1024];

			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}

			outputStream.close();
			inflater.end();

			return outputStream.toByteArray();
		} catch (Exception e) {
			Logger.error("An exception occurred while uncompressing with GZip: %s", e.getMessage());
			return null;
		}
	}
}
