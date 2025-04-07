package nl.tue.storage;

/**
 * Interface for the hash operation needed to store objects in the
 * CompressedStoreHashSet class. By contract, the hashcode returned for an
 * object should be consistent, i.e. the hashCode of the object retrieved from
 * the CompressedStore should equal the hashCode of the object before it was
 * stored;
 * 
 * @author bfvdonge
 * 
 * @param <K>
 */
public interface HashOperation<K> {

	public static class Default<K> implements HashOperation<K> {

		/**
		 * Return the hashCode of the object
		 * 
		 * @param object
		 * @return
		 */
		public int getHashCode(K object) {
			return object.hashCode();
		}

		/**
		 * Retrieve the object from the store and return its hashCode. Requires
		 * the store to have a non-null inflater
		 * 
		 * @param store
		 * @param l
		 * @return
		 * @throws StorageException
		 */
		public int getHashCode(CompressedStore<K> store, long l) throws StorageException {
			if (store.getInflater() == null) {
				throw new StorageException("Store does not have an inflater");
			}
			return store.getObject(l).hashCode();
		}
	};

	/**
	 * Returns the hashCode of the given object.
	 * 
	 * @param object
	 * @return
	 */
	public int getHashCode(K object);

	/**
	 * Returns the hashCode of the object stored in the store at index l.
	 * 
	 * @param store
	 * @param l
	 * @return
	 * @throws StorageException
	 */
	public int getHashCode(CompressedStore<K> store, long l) throws StorageException;

}
