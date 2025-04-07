package nl.tue.storage;

import java.io.IOException;

/**
 * Checks for equality between an object and a location in a CompressedStore.
 * 
 * 
 * @author bfvdonge
 * 
 */
public interface EqualOperation<K> {

	public static class Default<K> implements EqualOperation<K> {

		/**
		 * Check for equality between a given object and a location in the
		 * store. Should return true, if and only if the given object is indeed
		 * stored at index l in the store.
		 * 
		 * Default equality reads the entire object from the store and then
		 * calls equals on the given object. Requires the store to have an
		 * inflater.
		 * 
		 * @param object
		 * @param store
		 * @param l
		 * @return
		 * @throws StorageException
		 */
		public boolean equals(K object, CompressedStore<K> store, long l) throws StorageException {
			if (store.getInflater() == null) {
				throw new StorageException("Store does not have an inflater");
			}
			if (object == null) {
				return false;
			}
			return object.equals(store.getObject(l));

		}

	}

	/**
	 * Check for equality between a given object and a location in the store.
	 * Should return true, if and only if the given object is indeed stored at
	 * index l in the store.
	 * 
	 * @param object
	 * @param store
	 * @param l
	 * @return
	 * @throws StorageException
	 * @throws IOException
	 */
	public boolean equals(K object, CompressedStore<K> store, long l) throws StorageException, IOException;
}
