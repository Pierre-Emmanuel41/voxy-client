package fr.pederobien.voxy.client.impl.compressors;

import fr.pederobien.voxy.client.interfaces.ISampleCompressor;

public class NoCompression implements ISampleCompressor {

	@Override
	public int getAlgorithm() {
		return 0;
	}

	@Override
	public byte[] compress(byte[] toCompress) throws Exception {
		return toCompress;
	}

	@Override
	public byte[] decompress(byte[] toDecompress) throws Exception {
		return toDecompress;
	}
}
