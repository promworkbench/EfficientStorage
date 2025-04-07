package nl.tue.storage.compressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.tue.storage.CompressedStore;
import nl.tue.storage.Deflater;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;
import nl.tue.storage.Inflater;
import nl.tue.storage.StorageException;

/**
 * A CompressableCompressor implements all the necessary interfaces to
 * efficiently store objects in a CompressedHashSet.
 * 
 * @author bfvdonge
 * 
 * @param <C>
 */
public class CompressableCompressor<C extends Compressable> implements
		HashOperation<C>, EqualOperation<C>, Deflater<C>, Inflater<C> {

	private final Class<C> clazz;

	/**
	 * The compressor needs to be able to instantiate objects of the class <C>.
	 * Therefore, the class needs to be provided in the constructor and this
	 * class should have an empty constructor, i.e. a constructor without
	 * parameters.
	 * 
	 * @param clazz
	 */
	public CompressableCompressor(Class<C> clazz) {
		this.clazz = clazz;
	}

	public C inflate(InputStream stream) throws IOException {
		C object;
		try {
			object = clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		object.unCompress(stream);
		return object;
	}

	public void deflate(C object, OutputStream stream) throws IOException {
		object.compress(stream);
	}

	public boolean equals(C object, CompressedStore<C> store, long l)
			throws StorageException, IOException {
		if (object == null) {
			return false;
		}
		C stored = store.getObject(l);
		return object.equals(stored);
	}

	/**
	 * Return the hashCode of the object
	 * 
	 * @param object
	 * @return
	 */
	public int getHashCode(C object) {
		return object.hashCode();
	}

	/**
	 * Retrieve the object from the store and return its hashCode. Requires the
	 * store to have a non-null inflater
	 * 
	 * @param store
	 * @param l
	 * @return
	 * @throws StorageException
	 */
	public int getHashCode(CompressedStore<C> store, long l)
			throws StorageException {
		return store.getObject(l).hashCode();
	}

	@Override
	public int getMaxByteCount() {
		return -1;
	}

}
