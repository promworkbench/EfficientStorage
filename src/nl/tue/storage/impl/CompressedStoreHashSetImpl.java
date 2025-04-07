package nl.tue.storage.impl;

import java.io.IOException;

import nl.tue.storage.CompressedHashSet;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.Deflater;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;
import nl.tue.storage.Inflater;
import nl.tue.storage.StorageException;

public abstract class CompressedStoreHashSetImpl<K> implements
		CompressedHashSet<K> {

	public static final class IntCustomAlignment<K> extends
			CompressedStoreHashSetImpl<K> {

		public IntCustomAlignment(int alignment, Deflater<? super K> deflater,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			this(alignment, deflater, null, eq, hs, initialCapacity);
		}

		public IntCustomAlignment(int alignment, Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(alignment, deflater, inflater, alignment
					* (CompressedStore.BLOCKSIZE / alignment), eq, hs,
					initialCapacity);
		}

		public IntCustomAlignment(int alignment, Deflater<? super K> deflater,
				int blockSize, EqualOperation<K> eq, HashOperation<K> hs,
				int initialCapacity) {
			this(alignment, deflater, null, blockSize, eq, hs, initialCapacity);
		}

		public IntCustomAlignment(int alignment, Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				int initialCapacity) {
			this(alignment, deflater, inflater, alignment
					* (CompressedStore.BLOCKSIZE / alignment), eq,
					new HashOperation.Default<K>(), initialCapacity);
		}

		public IntCustomAlignment(int alignment, Deflater<? super K> deflater,
				Inflater<? extends K> inflater, HashOperation<K> hs,
				int initialCapacity) {
			this(alignment, deflater, inflater, alignment
					* (CompressedStore.BLOCKSIZE / alignment),
					new EqualOperation.Default<K>(), hs, initialCapacity);
		}

		public IntCustomAlignment(int alignment, Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int initialCapacity) {
			this(alignment, deflater, inflater, alignment
					* (CompressedStore.BLOCKSIZE / alignment),
					new EqualOperation.Default<K>(),
					new HashOperation.Default<K>(), initialCapacity);
		}

		public IntCustomAlignment(int alignment, Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			super(new AlignedIntBackedHashSet<K>(new CompressedStoreImpl<K>(
					alignment, deflater, inflater, blockSize), eq, hs,
					initialCapacity));
		}
	}

	public static final class Int32G<K> extends CompressedStoreHashSetImpl<K> {

		public Int32G(Deflater<? super K> deflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, null, eq, hs, initialCapacity);
		}

		public Int32G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq, hs,
					initialCapacity);
		}

		public Int32G(Deflater<? super K> deflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			this(deflater, null, blockSize, eq, hs, initialCapacity);
		}

		public Int32G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq,
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Int32G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, HashOperation<K> hs,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(), hs, initialCapacity);
		}

		public Int32G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(),
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Int32G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			super(new AlignedIntBackedHashSet<K>(new CompressedStoreImpl<K>(8,
					deflater, inflater, blockSize), eq, hs, initialCapacity));
		}
	}

	public static final class Int8G<K> extends CompressedStoreHashSetImpl<K> {

		public Int8G(Deflater<? super K> deflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, null, eq, hs, initialCapacity);
		}

		public Int8G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq, hs,
					initialCapacity);
		}

		public Int8G(Deflater<? super K> deflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			this(deflater, null, blockSize, eq, hs, initialCapacity);
		}

		public Int8G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq,
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Int8G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, HashOperation<K> hs,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(), hs, initialCapacity);
		}

		public Int8G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(),
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Int8G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			super(new AlignedIntBackedHashSet<K>(new CompressedStoreImpl<K>(2,
					deflater, inflater, blockSize), eq, hs, initialCapacity));
		}
	}

	public static final class Int4G<K> extends CompressedStoreHashSetImpl<K> {

		public Int4G(Deflater<? super K> deflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, null, eq, hs, initialCapacity);
		}

		public Int4G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq, hs,
					initialCapacity);
		}

		public Int4G(Deflater<? super K> deflater, int blockSize,
				int maxBlocks, EqualOperation<K> eq, HashOperation<K> hs,
				int initialCapacity) {
			this(deflater, null, blockSize, eq, hs, initialCapacity);
		}

		public Int4G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq,
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Int4G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, HashOperation<K> hs,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(), hs, initialCapacity);
		}

		public Int4G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(),
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Int4G(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			super(new AlignedIntBackedHashSet<K>(new CompressedStoreImpl<K>(1,
					deflater, inflater, blockSize), eq, hs, initialCapacity));
		}
	}

	public static final class Long<K> extends CompressedStoreHashSetImpl<K> {

		public Long(Deflater<? super K> deflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, null, eq, hs, initialCapacity);
		}

		public Long(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				HashOperation<K> hs, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq, hs,
					initialCapacity);
		}

		public Long(Deflater<? super K> deflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			this(deflater, null, blockSize, eq, hs, initialCapacity);
		}

		public Long(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, EqualOperation<K> eq,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE, eq,
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Long(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, HashOperation<K> hs,
				int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(), hs, initialCapacity);
		}

		public Long(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int initialCapacity) {
			this(deflater, inflater, CompressedStore.BLOCKSIZE,
					new EqualOperation.Default<K>(),
					new HashOperation.Default<K>(), initialCapacity);
		}

		public Long(Deflater<? super K> deflater,
				Inflater<? extends K> inflater, int blockSize,
				EqualOperation<K> eq, HashOperation<K> hs, int initialCapacity) {
			super(new LongBackedHashSet<K>(new CompressedStoreImpl<K>(deflater,
					inflater, blockSize), eq, hs, initialCapacity));
		}
	}

	public static final class Result<K> {
		public long index;
		public boolean isNew;
	}

	/**
	 * Local backed hashSet
	 */
	private final AbstractBackedHashSet<K> backingSet;

	public CompressedStoreHashSetImpl(AbstractBackedHashSet<K> backingSet) {
		this.backingSet = backingSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedHashSet#add(K)
	 */
	public Result<K> add(K val) throws StorageException {
		Result<K> result = new Result<K>();
		long index;
		try {
			index = backingSet.checkForAdd(val, true);
		} catch (IOException e) {
			throw new StorageException("Error while reading from storage: "
					+ e.getMessage());
		}
		result.isNew = index >= 0;
		result.index = result.isNew ? index : -(index + 1);

		return result; // yes, we added something
	}

	/**
	 * 
	 * The contains method is not synchronized. It may therefore produce false
	 * negatives, i.e. it may say that an object is not in the storage, while
	 * the object is already scheduled for storage. Therefore, any calls to
	 * contains that require a true answer should synchronize on this object.
	 */
	public long contains(K val) throws StorageException {
		long index;
		try {
			index = backingSet.checkForAdd(val, false);
		} catch (IOException e) {
			throw new StorageException("Error while reading from storage: "
					+ e.getMessage());

		}
		if (index >= 0) {
			return -1;
		} else {
			return -(index + 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedHashSet#getMemory()
	 */
	public long getMemory() {
		return 16 + 8 + 8 + 8 + 8 + backingSet.getStore().getMemory()
				+ backingSet.getMemory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.storage.CompressedHashSet#getObject(long)
	 */
	public K getObject(long l) throws StorageException {
		return backingSet.getStore().getObject(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.storage.CompressedHashSet#getBackingStore()
	 */
	public CompressedStore<K> getBackingStore() {
		return backingSet.getStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedHashSet#size()
	 */
	public int size() {
		return backingSet.size();
	}

	/**
	 * clears the storage space and removes all elements in the backing set.
	 */
	public void removeAll() {
		backingSet.removeAll();
	}
}
