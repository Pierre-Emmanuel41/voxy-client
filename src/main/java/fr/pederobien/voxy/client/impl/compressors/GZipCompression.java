package fr.pederobien.voxy.client.impl.compressors;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import fr.pederobien.voxy.client.interfaces.ISampleCompressor;

public class GZipCompression implements ISampleCompressor {

	@Override
	public int getAlgorithm() {
		return 1;
	}

	@Override
	public byte[] compress(byte[] toCompress) throws Exception {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		deflater.setInput(toCompress);
		deflater.finish();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(toCompress.length);
		byte[] buffer = new byte[1024];

		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}

		outputStream.close();
		deflater.end();

		return outputStream.toByteArray();
	}

	@Override
	public byte[] decompress(byte[] toDecompress) throws Exception {
		Inflater inflater = new Inflater();
		inflater.setInput(toDecompress);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(toDecompress.length);
		byte[] buffer = new byte[1024];

		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}

		outputStream.close();
		inflater.end();

		return outputStream.toByteArray();
	}
}
