package nl.tue.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nl.tue.storage.compressor.ByteListCompressor;
import nl.tue.storage.compressor.IntListCompressor;
import nl.tue.storage.compressor.LongListCompressor;
import nl.tue.storage.compressor.ShortListCompressor;
import nl.tue.storage.impl.CompressedStoreHashSetImpl;
import nl.tue.storage.impl.CompressedStoreHashSetImpl.Result;

public class StorageTest {

	private static final int STEPS = 1;
	// the length of the lists
	private static final int LENGTH = 2000;
	// the percentage of 0's
	private static final int PERCENTAGE = 80;
	// the number of vectors to generate (>= STEPS)
	private static final int VECTORS = 1;// * 1024 + 1;
	// blocksize in bytes
	private static final int BLOCKSIZE = 2 * 1024 * 1024;
	// Probability of a random vector being generated
	// from existing vectors
	private static final int DUPLICATE = 50;
	// type of arrays
	private static final Type TYPE = Type.S;
	// number of threads
	private static final int THREADS = 1;

	private static enum Type {
		I("integer (4 bytes)", 4), L("long (8 bytes)", 8), S("short (2 bytes)",
				2), B("byte (1 byte)", 1);
		private final String s;

		Type(String s, int i) {
			this.s = s;
		}

		public String toString() {
			return s;
		}

	};

	private static Random generator = new Random();

	public static List<Byte> randomByteArray(int length) {
		List<Byte> a = new ArrayList<Byte>(length);
		// for each item in the list
		for (int i = 0; i < length; i++) {
			// create a new random number and populate the
			// current location in the list with it
			if (generator.nextInt(100) < PERCENTAGE) {
				a.add((byte) 0);
			} else {
				a.add((byte) (generator.nextInt(Byte.MAX_VALUE)));
			}
		}
		return a;
	}

	public static List<Long> randomLongArray(int length) {
		List<Long> a = new ArrayList<Long>(length);
		// for each item in the list
		for (int i = 0; i < length; i++) {
			// create a new random number and populate the
			// current location in the list with it
			if (generator.nextInt(100) < PERCENTAGE) {
				a.add((long) 0);
			} else {
				a.add(generator.nextLong());
			}
		}
		return a;
	}

	public static List<Short> randomShortArray(int length) {
		List<Short> a = new ArrayList<Short>(length);
		// for each item in the list
		for (int i = 0; i < length; i++) {
			// create a new random number and populate the
			// current location in the list with it
			if (generator.nextInt(100) < PERCENTAGE) {
				a.add((short) 0);
			} else {
				a.add((short) ((generator.nextInt(Byte.MAX_VALUE) + 1) * (generator
						.nextInt(Byte.MAX_VALUE) + 1)));
			}
		}
		return a;
	}

	public static List<Integer> randomIntArray(int length) {
		List<Integer> a = new ArrayList<Integer>(length);
		// for each item in the list
		for (int i = 0; i < length; i++) {
			// create a new random number and populate the
			// current location in the list with it
			if (generator.nextInt(100) < PERCENTAGE) {
				a.add(0);
			} else {
				a.add(generator.nextInt(Integer.MAX_VALUE) + 1);
			}
		}
		return a;
	}

	public static void main(String[] args) throws StorageException {
		ErrorCount counter = new ErrorCount();
		for (int i = 0; i < 100; i++) {
			doTest(counter);
		}
		System.out.println("===============================");
		System.out.println(counter.count + " errors were found");
		System.exit(0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void doTest(final ErrorCount counter) throws StorageException {

		final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors
				.newFixedThreadPool(THREADS);

		final CompressedHashSet s;
		switch (TYPE) {
		case L:
			LongListCompressor c1 = new LongListCompressor(LENGTH);
			s = new CompressedStoreHashSetImpl.Int4G(c1, c1, BLOCKSIZE, c1, c1,
					10000);
			break;
		case I:
			IntListCompressor c2 = new IntListCompressor(LENGTH);
			s = new CompressedStoreHashSetImpl.Int4G(c2, c2, BLOCKSIZE, c2, c2,
					10000);
			break;
		case S:
			ShortListCompressor c3 = new ShortListCompressor(LENGTH);
			s = new CompressedStoreHashSetImpl.Int4G(c3, c3, BLOCKSIZE, c3, c3,
					10000);
			break;
		case B:
			ByteListCompressor c4 = new ByteListCompressor(LENGTH);
			s = new CompressedStoreHashSetImpl.Int4G(c4, c4, BLOCKSIZE, c4, c4,
					10000);
			break;
		default:
			s = null;

		}

		final StringBuffer[] buffers = new StringBuffer[THREADS + 1];
		buffers[0] = new StringBuffer();
		StringBuffer buff = buffers[0];

		buff.append("\n" + "Storing " + VECTORS + " random vectors of length "
				+ LENGTH + ",");
		buff.append("\n" + "containing elements of type " + TYPE);
		buff.append("\n" + "Around " + PERCENTAGE + "% of the values is 0.");
		final List<?>[] m = new List<?>[VECTORS];
		long start = System.currentTimeMillis();
		buff.append("\n" + "1/6 Randomizing arrays:          ");
		for (int i = 0; i < m.length; i++) {
			if (i % (VECTORS / STEPS) == 0) {
				buff.append(".");
			}
			if (i > 0 && generator.nextInt(100) < DUPLICATE) {
				m[i] = m[generator.nextInt(i)];
			} else {
				switch (TYPE) {
				case L:
					m[i] = randomLongArray(LENGTH);
					break;
				case I:
					m[i] = randomIntArray(LENGTH);
					break;
				case S:
					m[i] = randomShortArray(LENGTH);
					break;
				case B:
					m[i] = randomByteArray(LENGTH);
					break;
				}
			}
		}
		long end = System.currentTimeMillis();
		buff.append(" " + (end - start) / 1000.0 + " seconds.");

		final Set<Integer> codes = Collections
				.synchronizedSet(new HashSet<Integer>());

		for (int i = 0; i < THREADS; i++) {
			buffers[i + 1] = new StringBuffer();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e1) {
			}
			final int j = i + 1;
			pool.execute(new Runnable() {

				@Override
				public void run() {
					try {
						doTestInThread(counter, s, buffers[j], m, codes);
					} catch (StorageException e) {
						e.printStackTrace();
					}
				}

			});
		}
		pool.shutdown();
		while (!pool.isTerminated()) {
			try {
				pool.awaitTermination(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		}
		for (int i = 0; i < THREADS; i++) {
			System.out.print(buffers[0]);
			System.out.println(buffers[i + 1]);
		}
		System.out.println("Unique vectors:     " + codes.size());
		System.out.println("In efficient store: " + s.size());
		if (codes.size() != s.size()) {
			System.err.println("Error in sizes.");
			counter.count++;
		}

	}

	public static void doTestInThread(final ErrorCount counter,
			final CompressedHashSet<List<?>> s, StringBuffer buff, List<?>[] m,
			Set<Integer> codes) throws StorageException {

		final long[] index = new long[VECTORS];

		storeCompressed(counter, s, buff, m, codes, index);
		checkCompressed(counter, s, buff, m, codes, index);
		verifyCompressed(counter, s, buff, m, codes, index);
		Set<List<?>> v = storeJava(counter, s, buff, m, codes, index);
		validateJava(counter, s, buff, m, codes, index, v);

		// check if all items in the set are unique
		buff.append("\n" + "-------------------------------");
		// buff.append("\n" + "Memory in use by java hashset         : "
		// + toMBString(c * 4 + unique * 2 * 8 + mem));
		buff.append("\n" + "Memory in use by compressed hashset   : "
				+ toMBString(s.getMemory()));
		buff.append("\n" + "Currently allocated by backing store  : "
				+ toMBString(s.getBackingStore().getMemory()));
		buff.append("\n" + "     of which is wasted               : "
				+ toMBString(s.getBackingStore().getWastedMemory()));
		buff.append("\n" + "     of which is unused               : "
				+ toMBString(s.getBackingStore().getUnusedMemory()));
		buff.append("\n" + "-------------------------------");

	}

	private static String toMBString(long mem) {
		long mb = mem / 1024 / 1024;
		long kb = (mem - mb * 1024 * 1024) / 1024;
		long b = (mem - mb * 1024 * 1024 - kb * 1024);
		if (mb > 0) {
			return mb + " MB," + kb + " KB," + b + " B";
		}
		if (kb > 0) {
			return kb + " KB," + b + " B";
		}
		return b + " B";

	}

	private static void checkCompressed(final ErrorCount counter,
			final CompressedHashSet<List<?>> s, StringBuffer buff, List<?>[] m,
			Set<Integer> codes, long[] index) throws StorageException {
		long start = System.currentTimeMillis();
		buff.append("\n" + " Checking compressed store:   ");
		for (int i = 0; i < m.length; i++) {
			if (i % (VECTORS / STEPS) == 0) {
				buff.append(".");
			}
			long ret;
			// check containment
			synchronized (s) {
				ret = s.contains(m[i]);
			}
			if (index[i] != ret) {
				// System.err.print("Error while checking if vector "
				// + i +
				// " was already present in compressed set.");

				System.err.print("." + index[i] + "  " + ret + " : "
						+ s.getObject(index[i]).equals(s.getObject(ret)));
				counter.count++;
			}
		}
		long end = System.currentTimeMillis();
		buff.append(" " + (end - start) / 1000.0 + " seconds.");

	}

	private static void storeCompressed(final ErrorCount counter,
			final CompressedHashSet<List<?>> s, StringBuffer buff, List<?>[] m,
			Set<Integer> codes, long[] index) throws StorageException {
		long start = System.currentTimeMillis();
		buff.append("\n" + " Filling compressed store:    ");
		for (int i = 0; i < m.length; i++) {
			if (i % (VECTORS / STEPS) == 0) {
				buff.append(".");
			}
			Result<List<?>> added;
			synchronized (s) {
				added = s.add(m[i]);
			}
			index[i] = added.index;

		}

		long end = System.currentTimeMillis();
		buff.append(" " + (end - start) / 1000.0 + " seconds.");
		start = end;
	}

	private static void verifyCompressed(final ErrorCount counter,
			final CompressedHashSet<List<?>> s, StringBuffer buff, List<?>[] m,
			Set<Integer> codes, long[] index) throws StorageException {
		long start = System.currentTimeMillis();
		buff.append("\n" + " Verifying compressed store:  ");
		for (int i = 0; i < m.length; i++) {
			if (i % (VECTORS / STEPS) == 0) {
				buff.append(".");
			}
			if (!s.getObject(index[i]).equals(m[i])) {
				System.err.print(".");
				counter.count++;
			}

		}
		long end = System.currentTimeMillis();
		buff.append(" " + (end - start) / 1000.0 + " seconds.");
	}

	private static Set<List<?>> storeJava(final ErrorCount counter,
			final CompressedHashSet<List<?>> s, StringBuffer buff, List<?>[] m,
			Set<Integer> codes, long[] index) throws StorageException {
		long start = System.currentTimeMillis();
		buff.append("\n" + " Filling java HashSet:        ");

		Set<List<?>> v = new HashSet<List<?>>();
		for (int i = 0; i < m.length; i++) {
			if (i % (VECTORS / STEPS) == 0) {
				buff.append(".");
			}
			if (!v.contains(m[i])) {
				v.add(m[i]);
				codes.add(m[i].hashCode());
			}
		}
		long end = System.currentTimeMillis();
		buff.append(" " + (end - start) / 1000.0 + " seconds.");
		return v;
	}

	private static void validateJava(final ErrorCount counter,
			final CompressedHashSet<List<?>> s, StringBuffer buff, List<?>[] m,
			Set<Integer> codes, long[] index, Set<List<?>> v)
			throws StorageException {
		long start = System.currentTimeMillis();
		buff.append("\n" + " Checking Java HashSet:       ");
		for (int i = 0; i < m.length; i++) {
			if (i % (VECTORS / STEPS) == 0) {
				buff.append(".");
			}
			if (!v.contains(m[i])) {
				System.err.println("Error while checking if vector " + i
						+ " was already present in java set.");
			}
		}
		long end = System.currentTimeMillis();
		buff.append(" " + (end - start) / 1000.0 + " seconds.");
	}

	private static class ErrorCount {
		public volatile int count = 0;
	}
}
