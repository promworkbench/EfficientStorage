package nl.tue.storage;

import java.io.IOException;
import java.io.OutputStream;

public interface Deflater<T> {

	/**
	 * deflate the given object into the provided stream.
	 * 
	 * This method should NOT close the stream.
	 * 
	 * @param object
	 * @return
	 */
	public void deflate(T object, OutputStream stream) throws IOException;

	/**
	 * the method is called before each call to deflate. The call to deflate
	 * should not write more than the number of bytes returned by this method!
	 * It may write less.
	 * 
	 * If a negative value is returned, then a resizeable buffer is used
	 * 
	 * @return
	 */
	public int getMaxByteCount();

}
