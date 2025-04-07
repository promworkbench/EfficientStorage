package nl.tue.storage.impl;

import nl.tue.storage.CompressedStore;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;

class AlignedIntBackedHashSet<K> extends AbstractBackedHashSet<K> {

	private int[] pointerArray;
	private final int alignment;

	public AlignedIntBackedHashSet(CompressedStore<K> store, EqualOperation<K> eq, HashOperation<K> hs,
			int initialCapacity) {
		super(store, eq, hs, initialCapacity);
		this.alignment = store.getAlignment();

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
		pointerArray = new int[capacity];
		return capacity;
	}

	protected void rehash(int newCapacity) {
		int oldCapacity = pointerArray.length;

		int oldSet[] = pointerArray;
		// byte oldStates[] = _states;

		pointerArray = new int[newCapacity];
		// _states = new byte[newCapacity];

		for (int i = oldCapacity; i-- > 0;) {
			// if (oldStates[i] == FULL) {
			if (oldSet[i] != 0) {
				long o = int2long(oldSet[i]);
				try {
					insertKey(null, o, true);
				} catch (Exception e) {
					throw new RuntimeException("error while rehashing: C_old:" + oldCapacity + " C_new:" + newCapacity
							+ " o:" + o, e);
				}
			}
		}

	}

	protected int length() {
		return pointerArray.length;
	}

	protected long get(int i) {
		return int2long(pointerArray[i]);
	}

	protected void put(int i, long l) {
		pointerArray[i] = long2int(l);
	}

	protected void putEmpty(int i) {
		pointerArray[i] = 0;
	}

	protected boolean isEmpty(int i) {
		return pointerArray[i] == 0;
	}

	private long int2long(int i) {
		// long l = i;
		// l -= (long) Integer.MIN_VALUE;
		// l *= alignment;
		// l += 1;
		// assert (l > 0);
		// return l;
		// first, add maxint
		/*
		 * HV: The following snippet does not work correctly for some values.
		 */
//		int j = i;
//		j += Integer.MAX_VALUE;
//		j += 1;
//		long l = (long) j + (long) Integer.MAX_VALUE;
//		l *= alignment;
//		l += 1;
//		return l;
		/*
		 * HV: The following snippet (thanks to BVD) does work correctly for all values.
		 */
		long l = i&0xffffffffL;
		return (l-1)*alignment + 1;
	}

	private int long2int(long l) {
		// assert (l > 0);
		// l -= 1;
		// assert (l % alignment == 0);
		// l /= alignment;
		// l += (long)Integer.MIN_VALUE;
		// return (int) l;
		/*
		 * HV: The following snippet does not work correctly for some values.
		 */
//		l -= 1;
//		l /= alignment;
//		l -= Integer.MAX_VALUE;
//		int i = (int) l;
//		i -= 1;
//		i -= Integer.MAX_VALUE;
//		return i;
		/*
		 * HV: The following snippet (thanks to BVD) does work correctly for all values.
		 */
		l--;
		l /= alignment;
		if (l >= 0xffffffffL) {
			throw new OutOfMemoryError("[AlignedIntBackedHashSet] Attempt to store too many states. Trying to store index: " + (l*alignment));
		}
		return (int)((l+1) & 0xffffffffL);		
	}

	public long getMemory() {
		return 4 * pointerArray.length;
	}

	@Override
	protected boolean impliesEmpty(int i, long atIndex) {
		return atIndex <= 0;
	}

}
