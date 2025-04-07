package nl.tue.storage;

import nl.tue.storage.impl.SkippableOutputStream;

/**
 * Implementation of this compressed store is not thread safe. Hence writing by
 * multiple threads should be synchronized on this objects monitor. Since the
 * store is immutable, reading a previously written object back can be done
 * asynchronously and in parallel with writing7.
 * 
 * @author bfvdonge
 * 
 * @param <T>
 */

public interface CompressedStore<T> {

	/**
	 * size of each block. Defaults to 512 kBytes. Better to have more blocks
	 * when doing parallel processing
	 */
	public static final int BLOCKSIZE = 512 * 1024;

	/**
	 * Adds a deflated version of the provided object to the storate. The
	 * returned index is the location where this object can be retrieved from.
	 * 
	 * @param object
	 * @return the index of the first byte in the store
	 */
	public long addToStorage(T object) throws StorageException;

	/**
	 * Retrieves the byte representation of an object at a the given index
	 * 
	 * @param index
	 *            the start index of the object, provided when storing
	 * @return the byte[] representation of the object
	 */
	public FastByteArrayInputStream getStreamForObject(long index);

	/**
	 * This method returns an output stream for overwriting a previously stored
	 * object.
	 * 
	 * It is essential that this method is used with caution, as no checks are
	 * performed to avoid writing into the memory space of the next object!
	 * 
	 * @param index
	 * @return
	 */
	public SkippableOutputStream getOutputStreamForObject(long index);

	/**
	 * Retrieves the object at a the given index.
	 * 
	 * @param index
	 *            the start index of the object, provided when storing
	 * @return the object
	 * @throws StorageException
	 */
	public T getObject(long index) throws StorageException;

	/**
	 * Returns the number of bytes stored in the store
	 * 
	 * @return
	 */
	public long getSize();

	/**
	 * Returns the used memory by this storage
	 */
	public long getMemory();

	/**
	 * Returns the wasted memory by this storage
	 */
	public long getWastedMemory();

	/**
	 * Returns the unused memory by this storage
	 */
	public long getUnusedMemory();

	/**
	 * Returns the inflater of this store;
	 * 
	 * @return
	 */
	public Inflater<? extends T> getInflater();

	/**
	 * Returns the alignment of this store
	 * 
	 * @return
	 */
	public int getAlignment();

	/**
	 * Removes all elements from the store and resets it to it's initial state
	 */
	public void removeAll();

	/**
	 * Returns the number of blocks in use
	 * 
	 * @return
	 */
	public int getBlocksInUse();

}