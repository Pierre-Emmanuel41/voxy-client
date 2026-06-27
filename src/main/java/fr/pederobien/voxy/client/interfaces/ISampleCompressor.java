package fr.pederobien.voxy.client.interfaces;

public interface ISampleCompressor {

	/**
	 * @return The maximum size, in bytes, of the input buffer for compression. If the method returns -1, it means that there is no
	 *         constraint regarding the input buffer size.
	 */
	int getBytesPerFrame();

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
	 * @param buffer The bytes array that contains microphone's audio sample to compress.
	 * @return The compressed bytes array.
	 */
	byte[] compress(byte[] buffer) throws Exception;

	/**
	 * Performs the decompression of the given bytes array.
	 * 
	 * @param buffer The array that contains compressed audio sample.
	 * @return The decompressed array.
	 */
	byte[] decompress(byte[] buffer) throws Exception;
}
