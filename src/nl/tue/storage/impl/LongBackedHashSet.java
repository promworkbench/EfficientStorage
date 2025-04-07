package nl.tue.storage.impl;

import nl.tue.storage.CompressedStore;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;

class LongBackedHashSet<K> extends AbstractBackedHashSet<K> {

	public long[] pointerArray;

	public LongBackedHashSet(CompressedStore<K> store, EqualOperation<K> eq,
			HashOperation<K> hs, int initialCapacity) {
		super(store, eq, hs, initialCapacity);
	}

	protected long get(int i) {
		return pointerArray[i];
	}

	protected void put(int i, long l) {
		pointerArray[i] = l;
	}

	protected int length() {
		return pointerArray.length;
	}

	/**
	 * initializes the hashtable to a prime capacity which is at least
	 * <tt>initialCapacity + 1</tt>.
	 * 
	 * @param initialCapacity
	 *            an <code>int</code> value
	 * @return the actual capacity chosen
	 */
	protected int setUp(int initialCapacity) {
		int capacity;

		capacity = super.setUp(initialCapacity);
		pointerArray = new long[capacity];
		return capacity;
	}

	protected void rehash(int newCapacity) {
		int oldCapacity = pointerArray.length;

		long oldSet[] = pointerArray;
		// byte oldStates[] = _states;

		pointerArray = new long[newCapacity];
		// _states = new byte[newCapacity];

		for (int i = oldCapacity; i-- > 0;) {
			// if (oldStates[i] == FULL) {
			if (oldSet[i] != 0) {
				long o = oldSet[i];
				try {
					insertKey(null, o, true);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	protected void putEmpty(int i) {
		pointerArray[i] = 0;
	}

	protected boolean isEmpty(int i) {
		return pointerArray[i] == 0;
	}

	public long getMemory() {
		return 8 * pointerArray.length;
	}

	protected boolean impliesEmpty(int i, long atIndex) {
		return atIndex >= 0;
	}

}
