package nl.tue.storage;

import java.io.IOException;
import java.io.InputStream;

public interface Inflater<T> {

	/**
	 * read an object from a stream. The inflater should know when to stop
	 * reading and should NOT close the stream;
	 * 
	 * @param compressed
	 * @return
	 */
	public T inflate(InputStream stream) throws IOException;
}
