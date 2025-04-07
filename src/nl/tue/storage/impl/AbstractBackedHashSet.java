package nl.tue.storage.impl;

import gnu.trove.impl.HashFunctions;
import gnu.trove.impl.hash.THash;

import java.io.IOException;

import nl.tue.storage.CompressedStore;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;
import nl.tue.storage.StorageException;

abstract class AbstractBackedHashSet<K> extends THash {

	protected boolean consumeFreeSlot;

	public static final long EXISTFLAG = Integer.MAX_VALUE + 1l;
	private final CompressedStore<K> store;
	private final EqualOperation<K> eq;
	private final HashOperation<K> hs;

	protected final int initialCapacity;

	public AbstractBackedHashSet(CompressedStore<K> store,
			EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
		super(initialCapacity);
		this.store = store;
		this.eq = eq;
		this.hs = hs;
		this.initialCapacity = initialCapacity;

	}

	private int hashCode(long index) throws StorageException {
		return hs.getHashCode(store, index - 1);
	}

	private int hashCode(K object) {
		return hs.getHashCode(object);
	}

	private boolean equals(K val, long l) throws StorageException, IOException {
		return eq.equals(val, store, l - 1);
	}

	/**
	 * Returns the index of the given object in the backing store. If the object
	 * existed before the call to add, a negative index is returned, otherwise a
	 * positive index is returned.
	 * 
	 * @param val
	 * @param addIfNew
	 *            indicates if the value should be added if it is new
	 * @return a negative index if the value was there. The actual index can be
	 *         obtainen by -(result + 1) a non-negative index if the value was
	 *         not there. If it was added, then it was added at the returned
	 *         index.
	 * @throws StorageException
	 * @throws IOException
	 */
	long checkForAdd(K val, boolean addIfNew) throws StorageException,
			IOException {
		long existing;
		long index;
		// we need to synchronize here, otherwise the result of the
		// get() method on the returned index, may return something
		// different than the value at index according to inds
		//
		index = insertKey(val, -1, addIfNew);
		if (index >= 0 && (index & EXISTFLAG) == EXISTFLAG) {
			existing = get((int) (index ^ EXISTFLAG)) - 1;
		} else if (addIfNew) {
			existing = get((int) index) - 1;
		} else {
			existing = 0;
		}

		if (index >= 0 && (index & EXISTFLAG) == EXISTFLAG) {
			// already present in set, nothing to add, so return
			// the stored long as a negative number
			return -existing - 1;
		}

		if (addIfNew) {
			postInsertHook(consumeFreeSlot);
			return existing;// _set[index]; // yes, we added something
		} else {
			return 0;// we checked and the object is not in here yet
		}
	}

	/**
	 * Locates the index at which <tt>val</tt> can be inserted. if there is
	 * already a value equal()ing <tt>val</tt> in the set, returns that value as
	 * a negative integer.
	 * 
	 * @param val
	 *            an <code>long</code> value
	 * @return an <code>int</code> value
	 * @throws StorageException
	 * @throws IOException
	 */
	protected long insertKey(K val, long idx, boolean addIfNew)
			throws StorageException, IOException {
		assert (val != null || idx != 0);
		int hash, index;
		if (val == null) {
			hash = hashCode(idx) & 0x7fffffff;
		} else {
			hash = hashCode(val) & 0x7fffffff;
		}
		index = hash % length();
		long atIndex = get(index);

		// byte state = _states[index];

		consumeFreeSlot = false;

		if (impliesEmpty(index, atIndex)) {
			consumeFreeSlot = addIfNew;
			// store the object in the backing store
			// store the index here
			if (addIfNew) {
				put(index, store(val, idx));
			}

			return index; // empty, all done
		} else if (idx == atIndex || (val != null && equals(val, atIndex))) {
			return EXISTFLAG | index; // already stored
		}

		// already FULL or REMOVED, must probe
		return insertKeyRehash(val, idx, index, hash, addIfNew);
	}

	private long insertKeyRehash(K val, long idx, int index, int hash,
			boolean addIfNew) throws StorageException, IOException {
		assert (val != null || idx >= 0);
		// compute the double hash
		final int length = length();
		int probe = 1 + (hash % (length - 2));
		final int loopIndex = index;

		/**
		 * Look until FREE slot or we start to loop
		 */
		do {

			index -= probe;
			if (index < 0) {
				index += length;
			}
			long atIndex = get(index);
			// state = _states[index];

			// A FREE slot stops the search
			if (impliesEmpty(index, atIndex)) {
				// store the object in the backing store
				// store the index here
				if (addIfNew) {
					put(index, store(val, idx));
				}
				return index;

			} else if (idx == atIndex || (val != null && equals(val, atIndex))) {
				return EXISTFLAG | index;
			}

			// Detect loop
		} while (index != loopIndex);

		// Can a resizing strategy be found that resizes the set?
		throw new IllegalStateException(
				"No free or removed slots available. Key set full?!!");
	}

	private long store(K val, long idx) throws StorageException {
		if (idx >= 0) {
			return idx;
		}
		return store.addToStorage(val) + 1;
	}

	public int capacity() {
		return length();
	}

	protected abstract long get(int i);

	protected abstract void put(int i, long l);

	protected abstract int length();

	protected abstract void putEmpty(int i);

	// Check if the long value stored at index i represents empty
	protected abstract boolean impliesEmpty(int i, long atIndex);

	public CompressedStore<K> getStore() {
		return store;
	}

	public abstract long getMemory();

	void removeAll() {
		setUp(HashFunctions.fastCeil(initialCapacity / _loadFactor));
		clear();
		store.removeAll();
	}
}
