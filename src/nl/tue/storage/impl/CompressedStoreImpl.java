package nl.tue.storage.impl;

import java.io.IOException;

import nl.tue.storage.CompressedStore;
import nl.tue.storage.Deflater;
import nl.tue.storage.FastByteArrayInputStream;
import nl.tue.storage.FastByteArrayOutputStream;
import nl.tue.storage.Inflater;
import nl.tue.storage.ResizeableFastByteArrayOutputStream;
import nl.tue.storage.StorageException;

public class CompressedStoreImpl<T> implements CompressedStore<T> {

	private static final int DEFAULTALIGNMENT = 1;

	/**
	 * internal blocksize
	 */
	private final int blockSize;

	/**
	 * The actual store in which bytes are stored.
	 */
	private byte[][] store;

	/**
	 * current size of the storage, also the first index in which I can store
	 */
	private volatile long size;

	/**
	 * current number of blocks
	 */
	private int blocks;

	/**
	 * current number of wasted bytes
	 */
	private long wasted;

	/**
	 * deflater used to deflate an object into a byte array
	 * 
	 */
	private final Deflater<? super T> deflater;

	/**
	 * inflater used to inflate the byte array to an object, if provided
	 */
	private final Inflater<? extends T> inflater;

	/**
	 * byte alignment
	 */
	private final int alignment;

	public CompressedStoreImpl(Deflater<? super T> deflater) {
		this(deflater, null);
	}

	public CompressedStoreImpl(Deflater<? super T> deflater, Inflater<? extends T> inflater) {
		this(deflater, inflater, BLOCKSIZE);
	}

	public CompressedStoreImpl(Deflater<? super T> deflater, Inflater<? extends T> inflater, int blockSize) {
		this(DEFAULTALIGNMENT, deflater, inflater, blockSize);
	}

	public CompressedStoreImpl(int alignment, Deflater<? super T> deflater) {
		this(alignment, deflater, null);
	}

	public CompressedStoreImpl(int alignment, Deflater<? super T> deflater, Inflater<? extends T> inflater) {
		this(alignment, deflater, inflater, BLOCKSIZE);
	}

	public CompressedStoreImpl(int alignment, Deflater<? super T> deflater, Inflater<? extends T> inflater,
			int blockSize) {
		this.alignment = alignment;
		this.deflater = deflater;
		this.inflater = inflater;
		// Blocksize needs to be an exact multiple of alignment, with a minimum of 8
		this.blockSize = blockSize / alignment >= 8 ? alignment * (blockSize / alignment) : 8 * alignment;
		removeAll();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedStore#addToStorage(T)
	 */
	public long addToStorage(T object) throws StorageException {

		final long startIndex;
		final int s;
		final int cnt = deflater.getMaxByteCount();
		final FastByteArrayOutputStream out;
		if (cnt < 0) {
			out = new ResizeableFastByteArrayOutputStream(256);
		} else {
			out = new FastByteArrayOutputStream(cnt);
		}
		// here, synchronization on this is needed to obtain the startindex
		// for
		// writing and setting the new size, before asynchronously copying
		// in
		// the
		// contents. The blocking while reading the stream is needed to make
		// sure not to threads are storing the same object in parallel,
		// leading
		// to two instances of an object in the store.
		//
		try {
			deflater.deflate(object, out);
		} catch (IOException e) {
			throw new StorageException(e.getMessage());
		}
		if (out.getSize() > blockSize) {
			// throw an exception, as we cannot store this object into
			// a single array.
			throw new StorageException("Object too large to be stored, increase the "
					+ "blocksize of the storage and try again");
		}
		s = out.getSize();
		int blockIndex, block;
		synchronized (this) {
			if ((size + s) / blockSize >= blocks) {
				if (blocks == store.length) {
					if (blocks > Integer.MAX_VALUE / 2) {
						throw new StorageException("Storage Full");
					}
					// double the storage.
					// byte[][] oldStore = store;
					// store = new byte[2 * blocks][];
					// System.arraycopy(oldStore, 0, store, 0, blocks);
					//
					byte[][] newStore = new byte[2 * blocks][];
					System.arraycopy(store, 0, newStore, 0, blocks);
					store = newStore;

				}
				// we need to add a block
				store[blocks] = new byte[blockSize];
				// make sure we write the whole stream into one block
				wasted += ((long) blocks) * blockSize - size;
				// there is a need for the two steps here to avoid the product
				// to
				// go beyond the range of integers and then be casted to long.
				size = blocks;
				size *= blockSize;
				blocks++;
			}

			startIndex = size;

			size += s;

			// check the alignment
			if (size % alignment != 0) {
				long w = alignment - (size % alignment);
				size += w;
				wasted += w;
			}
			// now copy the byte array into the store
			blockIndex = (int) (startIndex % blockSize);
			block = (int) (startIndex / blockSize);
		}

		byte[] deflated = out.getByteArray();
		synchronized (store[block]) {
			System.arraycopy(deflated, 0, store[block], blockIndex, s);
		}
		return startIndex;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.storage.CompressedStore#getStreamForObject
	 * (long)
	 */
	public FastByteArrayInputStream getStreamForObject(long index) {
		int block = (int) (index / blockSize);
		int blockIndex = (int) (index % blockSize);
		return new FastByteArrayInputStream(store[block], blockIndex, blockSize - blockIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedStore#getObject(long)
	 */
	public T getObject(long index) throws StorageException {
		if (inflater == null) {
			throw new StorageException("No inflater specified.");
		}
		try {
			FastByteArrayInputStream stream = getStreamForObject(index);
			synchronized (stream.getLock()) {
				return inflater.inflate(stream);
			}
		} catch (IOException e) {
			throw new StorageException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedStore#getSize()
	 */
	public long getSize() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedStore#getMemory()
	 */
	public long getMemory() {
		return 8 + 4 + 4 + 8 + 8 + 4 + 8 + 8 + 8 + 8 + 8 + 24 + blocks * (24 + blockSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.storage.CompressedStore#getWastedMemory()
	 */
	public long getWastedMemory() {
		return wasted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.storage.CompressedStore#getUnusedMemory()
	 */
	public long getUnusedMemory() {
		return (blockSize - size % blockSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.storage.CompressedStore#getInflater()
	 */
	public Inflater<? extends T> getInflater() {
		return inflater;
	}

	public int getAlignment() {
		return alignment;
	}

	@Override
	public SkippableOutputStream getOutputStreamForObject(long index) {
		final int block = (int) (index / blockSize);
		final int blockIndex = (int) (index % blockSize);
		return new SkippableOutputStream(store[block], blockIndex);
	}

	@Override
	public void removeAll() {
		this.blocks = 0;
		this.size = 0;
		this.wasted = 0;
		synchronized (this) {
			this.store = new byte[8][];
		}
	}

	public int getBlocksInUse() {
		return blocks;
	}

}
