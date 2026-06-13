package fr.pederobien.voxy.client.interfaces;

public interface ISampleCompressor {

	/**
	 * Values in range [0,255]. However the first 20 values reserved by this application for further compression algorithm
	 * implementation.<br>
	 * 0 - No compression<br>
	 * 1 - GZip algorithm<br>
	 * 
	 * @return The algorithm number associated to this compressor. It is used as a key to uncompress the sample when received by the
	 *         other players.
	 */
	int getAlgorithm();

	/**
	 * Performs the compression of the given bytes array.
	 * 
	 * @param toCompress The bytes array that contains microphone's audio sample to compress.
	 * @return The compressed bytes array.
	 */
	byte[] compress(byte[] toCompress) throws Exception;

	/**
	 * Performs the decompression of the given bytes array.
	 * 
	 * @param toDecompress The array that contains compressed audio sample.
	 * @return The decompressed array.
	 */
	byte[] decompress(byte[] toDecompress) throws Exception;
}
