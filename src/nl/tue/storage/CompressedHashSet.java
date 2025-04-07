package nl.tue.storage;

import nl.tue.storage.impl.CompressedStoreHashSetImpl.Result;

/**
 * The implementation is not Thread-safe. For thread safety in a multi-threaded
 * environment, both the add and contains method should only be called when
 * holding a lock on this object's monitor.
 * 
 * The getObject() method does not have to be synchronized, since the underlying
 * storage is considered immutable and once an index is returned by the add
 * method, this index can be read concurrently.
 * 
 * Read access to the backing store can also be a-synchronous, as long as
 * reading is done only from indices previously added.
 * 
 * @author bfvdonge
 * 
 * @param <K>
 */
public interface CompressedHashSet<K> {

	/**
	 * Returns the index of the given object in the backing store. If the object
	 * existed before the call to add, a negative index is returned, otherwise a
	 * positive index is returned.
	 * 
	 * @param val
	 * @return
	 * @throws StorageException
	 */
	public Result<K> add(K val) throws StorageException;

	/**
	 * Returns the index of the given object in the backing store. If the object
	 * existed before the call to add, a non-negative index is returned,
	 * otherwise -1 is returned.
	 * 
	 * @param val
	 * @return
	 * @throws StorageException
	 */
	public long contains(K val) throws StorageException;

	/**
	 * returns the memory use of the backing store plus the size of the
	 * backingSet.
	 * 
	 * @return
	 */
	public long getMemory();

	/**
	 * Return the original object stored at index l in the backing storage.
	 * 
	 * @param l
	 * @return
	 * @throws StorageException
	 */
	public K getObject(long l) throws StorageException;

	/**
	 * Returns the CompressedStore used by this Set.
	 * 
	 * @return
	 */
	public CompressedStore<K> getBackingStore();

	/**
	 * return the number of objects stored in this set
	 * 
	 * @return
	 */
	public int size();

	/**
	 * clears the storage space and removes all elements in the backing set.
	 */
	public void removeAll();

}