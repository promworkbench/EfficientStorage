package nl.tue.storage.compressor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.tue.storage.CompressedStore;
import nl.tue.storage.Deflater;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;
import nl.tue.storage.Inflater;
import nl.tue.storage.StorageException;

/**
 * Abstract base class for vector storage
 * 
 * Note that the hashCode operation does not just look at the bitmask, but the
 * equals does. If the hashCode operation would only consider the bitmask, then
 * all vectors with the same bitmask would end up in the same bin in the hashSet
 * and this would require each hash collision to use the full equality, rather
 * than the quick check on the bitmasks.
 * 
 * It is required that each list that is stored in a CompressedStore using an
 * instance of this class has the same length.
 * 
 * @author bfvdonge
 * 
 * @param <K>
 */
public abstract class AbstractFixedLengthListCompressor<K extends Number>
		implements EqualOperation<List<K>>, Deflater<List<K>>,
		Inflater<List<K>>, HashOperation<List<K>> {

	protected final int length;

	/**
	 * Construct the compressor with a fixed length for all vectors.
	 * 
	 * @param length
	 */
	public AbstractFixedLengthListCompressor(int length) {
		this.length = length;

	}

	/**
	 * Indicates if the given number equals 0
	 * 
	 * @param number
	 * @return
	 */
	protected abstract boolean isZero(K number);

	/**
	 * returns the number of bytes for storing one object of type K
	 * 
	 * @return
	 */
	protected abstract int numBytes();

	/**
	 * writes an object of type K to an array of bytes, of size numBytes();
	 * 
	 * @param number
	 * @return
	 */
	protected abstract byte[] toByteArray(K number);

	/**
	 * reads an object of type K from an array of bytes of size numBytes()
	 * 
	 * @param bytes
	 * @return
	 */
	protected abstract K fromByteArray(byte[] bytes);

	/**
	 * Instantiates a new array of the given length, such that for all elements,
	 * isZero() returns true.
	 * 
	 * @param length
	 * @return
	 */
	protected abstract K[] newEmptyArray(int length);

	public List<K> inflate(InputStream stream) throws IOException {
		BitMask mask = readMask(stream);
		return inflateContent(stream, BitMask.getIndices(mask));

	}

	protected BitMask readMask(InputStream stream) throws IOException {
		byte[] mask = new byte[BitMask.getNumBytes(length)];
		stream.read(mask);
		return new BitMask(mask, length);
	}

	protected List<K> inflateContent(InputStream stream, int[] ids)
			throws IOException {

		K[] v = newEmptyArray(length);
		for (int i : ids) {
			byte[] buffer = new byte[numBytes()];
			stream.read(buffer);
			v[i] = fromByteArray(buffer);
		}
		assert (v.length == length);
		return Arrays.asList(v);
	}

	public void deflate(List<K> object, OutputStream stream) throws IOException {
		assert (object.size() == length);
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < object.size(); i++) {
			if (!isZero(object.get(i))) {
				ids.add(i);
			}
		}
		stream.write(BitMask.makeBitMask(length, ids).getBytes());
		for (int i : ids) {
			stream.write(toByteArray(object.get(i)));
		}

	}

	protected void deflateHead(List<K> object, OutputStream stream)
			throws IOException {
		assert (object.size() == length);
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < object.size(); i++) {
			if (!isZero(object.get(i))) {
				ids.add(i);
			}
		}
		stream.write(BitMask.makeBitMask(length, ids).getBytes());
	}

	protected BitMask getBitMask(List<K> vector) {
		assert (vector.size() == length);
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < vector.size(); i++) {
			if (!isZero(vector.get(i))) {
				ids.add(i);
			}
		}
		return BitMask.makeBitMask(length, ids);
	}

	public boolean equals(List<K> vector, CompressedStore<List<K>> store, long l)
			throws StorageException, IOException {
		if (vector == null) {
			return false;
		}
		assert (vector.size() == length);
		// First compare the bitMasks and only then the entire object
		BitMask bmv = getBitMask(vector);
		InputStream stream = store.getStreamForObject(l);
		BitMask bms = readMask(stream);

		if (!bmv.equals(bms)) {
			// quick check on the bitmasks
			return false;
		}
		// now, check the values.
		List<K> inStore = inflateContent(stream, BitMask.getIndices(bms));

		return vector.equals(inStore);
	}

	/**
	 * Return the hashCode of the object
	 * 
	 * @param object
	 * @return
	 */
	public int getHashCode(List<K> object) {
		List<Integer> ids = new ArrayList<Integer>();
		for (int i = 0; i < object.size(); i++) {
			if (!isZero(object.get(i))) {
				ids.add(i);
			}
		}
		BitMask m1 = BitMask.makeBitMask(length, ids);
		return m1.hashCode();
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
	public int getHashCode(CompressedStore<List<K>> store, long l)
			throws StorageException {
		if (store.getInflater() == null) {
			throw new StorageException("Store does not have an inflater");
		}
		try {
			InputStream stream = store.getStreamForObject(l);
			byte[] mask = new byte[BitMask.getNumBytes(length)];
			stream.read(mask);
			return mask.hashCode();
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}

}
