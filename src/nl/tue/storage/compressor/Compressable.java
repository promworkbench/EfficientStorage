package nl.tue.storage.compressor;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementations of this interface should have an empty contructor. The
 * CompressableListCompressor uses that contructor to construct a new object,
 * after which the method unCompress(stream) is called on that empty object.
 * 
 * @author bfvdonge
 * 
 */
public interface Compressable {

	/**
	 * Write this object to the stream
	 * 
	 * @param stream
	 */
	public void compress(OutputStream stream);

	/**
	 * Read this object from the given stream. The object should know when to
	 * stop reading, i.e. the stream may be larger than the necessary for this
	 * object.
	 * 
	 * @param stream
	 * @return
	 */
	public Compressable unCompress(InputStream stream);

}
