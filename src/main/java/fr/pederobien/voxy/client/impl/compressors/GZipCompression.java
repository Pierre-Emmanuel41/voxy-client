package fr.pederobien.voxy.client.impl.compressors;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import fr.pederobien.voxy.client.interfaces.ISampleCompressor;

public class GZipCompression implements ISampleCompressor {

	@Override
	public int getBytesPerFrame() {
		return -1;
	}

	@Override
	public int getAlgorithm() {
		return 1;
	}

	@Override
	public byte[] compress(byte[] buffer) throws Exception {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		deflater.setInput(buffer);
		deflater.finish();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buffer.length);
		byte[] compressed = new byte[1024];

		while (!deflater.finished()) {
			int count = deflater.deflate(compressed);
			outputStream.write(compressed, 0, count);
		}

		outputStream.close();
		deflater.end();

		return outputStream.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] buffer) throws Exception {
		Inflater inflater = new Inflater();
		inflater.setInput(buffer);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buffer.length);
		byte[] decompressed = new byte[1024];

		while (!inflater.finished()) {
			int count = inflater.inflate(decompressed);
			outputStream.write(decompressed, 0, count);
		}

		outputStream.close();
		inflater.end();

		return outputStream.toByteArray();
	}
}
