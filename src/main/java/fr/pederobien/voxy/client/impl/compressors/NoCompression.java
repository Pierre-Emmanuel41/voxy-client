package fr.pederobien.voxy.client.impl.compressors;

import fr.pederobien.voxy.client.interfaces.ISampleCompressor;

public class NoCompression implements ISampleCompressor {

	@Override
	public int getBytesPerFrame() {
		return -1;
	}

	@Override
	public int getAlgorithm() {
		return 0;
	}

	@Override
	public byte[] compress(byte[] buffer) throws Exception {
		return buffer;
	}

	@Override
	public byte[] decompress(byte[] buffer) throws Exception {
		return buffer;
	}
}
