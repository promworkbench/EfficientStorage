package nl.tue.storage.hashing;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;

import java.util.Random;

import nl.tue.storage.hashing.impl.AdHashHashCodeProvider;
import nl.tue.storage.hashing.impl.BernsteinHashCodeProvider;
import nl.tue.storage.hashing.impl.FNV1HashCodeProvider;
import nl.tue.storage.hashing.impl.FNV1aHashCodeProvider;
import nl.tue.storage.hashing.impl.JavaHashCodeProvider;
import nl.tue.storage.hashing.impl.JenkingsHashCodeProvider;
import nl.tue.storage.hashing.impl.MurMur3HashCodeProvider;
import nl.tue.storage.hashing.impl.OneAtATimeHashCodeProvider;

public class HashCodeProviderTest {

	/*-
	 * Extensive testing has shown that the MurMur3 hash has the best collision
	 * rate for the time. The incremental hash is faster, but it behaves
	 * unpredicably.
	 * 
	 * Standard java implementations are very fast, but completely stupid,
	 * reaching collision rates over 85%, see the example output below.
	 *
	 * Collision test on 33554432 semi-random arrays.
	 * Expecting 130731.32841730118 collisions.
	 * JAVA31     ,12.08 ,seconds,130721,collisions
	 * Bernstein  ,12.23 ,seconds,130428,collisions
	 * MurMur3    ,12.46 ,seconds,131519,collisions
	 * Jenkings   ,23.062,seconds,131031,collisions
	 * 
	 * FNV1       ,14.10 ,seconds,130695,collisions
	 * FNV1a      ,14.01 ,seconds,130606,collisions
	 * OAT        ,16.97 ,seconds,130546,collisions
	 * 
	 * Incremental, 9.41 ,seconds,238423,collisions
	 */

	private final static HashCodeProvider[] PROVIDERS = new HashCodeProvider[] {
			new AdHashHashCodeProvider()

			, new FNV1HashCodeProvider(), new FNV1aHashCodeProvider(),
			new OneAtATimeHashCodeProvider(), new MurMur3HashCodeProvider(),
			new JenkingsHashCodeProvider(), new BernsteinHashCodeProvider(),
			new JavaHashCodeProvider() //
	};

	// private final static HashCodeProvider[] PROVIDERS = new
	// HashCodeProvider[] {
	// new AdHashHashCodeProvider(), new MurMur3HashCodeProvider() };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int col = 1 << 20;
		int length = 150;
		int numAdjust = 5; // not inclusive
		int maxAdjust = 2; // not inclusive
		boolean assertions = true;
		doTestShort(col, length, numAdjust, maxAdjust, assertions);
		doTestInt(col, length, numAdjust, maxAdjust, assertions);
	}

	public static void doTestShort(int col, int length, int numAdjust,
			int maxAdjust, boolean assertions) {

		System.out
				.println("Collision test on  " + col + " semi-random arrays.");
		System.out.println("Expecting " + expectedCol(col, 1L << 32)
				+ " collisions.");
		Random r = new Random();
		TIntIntMap codes = new TIntIntHashMap(2 * col);

		for (int p = 0; p < PROVIDERS.length; p++) {

			if (!(PROVIDERS[p] instanceof IncrementalHashCodeProvider)) {
				continue;
			}
			IncrementalHashCodeProvider prov = (IncrementalHashCodeProvider) PROVIDERS[p];
			short[] array = getRandomArrayShort(r, length);
			long start = System.currentTimeMillis();
			int hash = prov.hash(array);
			int cnt = 0;
			for (int i = 0; i < col; i++) {
				int ch = r.nextInt(numAdjust) + 1;
				for (int c = 0; c < ch; c++) {
					short index = (short) r.nextInt(length);
					int adjust = r.nextInt(maxAdjust) + 1;// - 2;
					short oldVal = array[index];
					array[index] = (short) Math.max((short) (oldVal + adjust),
							(short) 0);
					hash = prov.updateHash(hash, index, oldVal, array[index]);
					assert !assertions || hash == prov.hash(array);
				}

				if (codes.adjustOrPutValue(hash, 1, 1) > 1) {
					cnt++;
				}

			}
			long end = System.currentTimeMillis();
			System.out.println("Short,I," + prov.toString() + ","
					+ (end - start) / 1000.0 + ",seconds," + //
					cnt + ",collisions," + codes.size() + ",unique");
			// System.out.println("  Chi square: " + computeChiSq(col, 1L << 32,
			// codes));

			codes.clear();
		}

		for (int p = 0; p < PROVIDERS.length; p++) {
			short[] array = getRandomArrayShort(r, length);

			long start = System.currentTimeMillis();
			int cnt = 0;
			for (int i = 0; i < col; i++) {
				int ch = r.nextInt(numAdjust) + 1;
				for (int c = 0; c < ch; c++) {
					short index = (short) r.nextInt(length);
					int adjust = r.nextInt(maxAdjust) + 1;// - 2;
					short oldVal = array[index];
					array[index] = (short) Math.max((short) (oldVal + adjust),
							(short) 0);
				}

				if (codes.adjustOrPutValue(PROVIDERS[p].hash(array), 1, 1) > 1) {
					cnt++;
				}

			}
			long end = System.currentTimeMillis();
			System.out.println("Short,N," + PROVIDERS[p].toString() + ","
					+ (end - start) / 1000.0 + ",seconds," + //
					cnt + ",collisions," + codes.size() + ",unique");
			// System.out.println("  Chi square: " + computeChiSq(col, 1L << 32,
			// codes));

			codes.clear();
		}

		System.out.println("Done.");

	}

	private static short[] getRandomArrayShort(Random r, int length) {
		short[] array = new short[length];
		for (int j = 0; j < length; j++) {
			array[j] = (short) r.nextInt(10);
			if (array[j] > 5) {
				array[j] = 0;
			}
		}
		return array;
	}

	public static void doTestInt(int col, int length, int numAdjust,
			int maxAdjust, boolean assertions) {

		System.out
				.println("Collision test on  " + col + " semi-random arrays.");
		System.out.println("Expecting " + expectedCol(col, 1L << 32)
				+ " collisions.");
		Random r = new Random();
		TIntIntMap codes = new TIntIntHashMap(2 * col);

		for (int p = 0; p < PROVIDERS.length; p++) {

			if (!(PROVIDERS[p] instanceof IncrementalHashCodeProvider)) {
				continue;
			}
			IncrementalHashCodeProvider prov = (IncrementalHashCodeProvider) PROVIDERS[p];
			int[] array = getRandomArrayInt(r, length);
			long start = System.currentTimeMillis();
			int hash = prov.hash(array);
			int cnt = 0;
			for (int i = 0; i < col; i++) {
				int ch = r.nextInt(numAdjust) + 1;
				for (int c = 0; c < ch; c++) {
					short index = (short) r.nextInt(length);
					int adjust = r.nextInt(maxAdjust) + 1;// - 2;
					int oldVal = array[index];
					array[index] = Math.max((oldVal + adjust), 0);
					hash = prov.updateHash(hash, index, oldVal, array[index]);
					assert !assertions || hash == prov.hash(array);
				}

				if (codes.adjustOrPutValue(hash, 1, 1) > 1) {
					cnt++;
				}

			}
			long end = System.currentTimeMillis();
			System.out.println("Int,I," + prov.toString() + "," + (end - start)
					/ 1000.0 + ",seconds," + //
					cnt + ",collisions," + codes.size() + ",unique");
			// System.out.println("  Chi square: " + computeChiSq(col, 1L << 32,
			// codes));

			codes.clear();
		}

		for (int p = 0; p < PROVIDERS.length; p++) {
			int[] array = getRandomArrayInt(r, length);

			long start = System.currentTimeMillis();
			int cnt = 0;
			for (int i = 0; i < col; i++) {
				int ch = r.nextInt(numAdjust) + 1;
				for (int c = 0; c < ch; c++) {
					short index = (short) r.nextInt(length);
					int adjust = r.nextInt(maxAdjust) + 1;// - 2;
					int oldVal = array[index];
					array[index] = Math.max((oldVal + adjust), 0);
				}

				if (codes.adjustOrPutValue(PROVIDERS[p].hash(array), 1, 1) > 1) {
					cnt++;
				}

			}
			long end = System.currentTimeMillis();
			System.out.println("Int,N," + PROVIDERS[p].toString() + ","
					+ (end - start) / 1000.0 + ",seconds," + //
					cnt + ",collisions," + codes.size() + ",unique");
			// System.out.println("  Chi square: " + computeChiSq(col, 1L << 32,
			// codes));

			codes.clear();
		}

		System.out.println("Done.");

	}

	private static int[] getRandomArrayInt(Random r, int length) {
		int[] array = new int[length];
		for (int j = 0; j < length; j++) {
			array[j] = r.nextInt(10);
			if (array[j] > 5) {
				array[j] = 0;
			}
		}
		return array;
	}

	// protected static class HashedObject {
	//
	// private final short[] array;
	// private final int hashCode;
	//
	// public HashedObject(short[] array, int hashCode) {
	// this.array = array;
	// this.hashCode = hashCode;
	// }
	//
	// public HashedObject(short[] array, HashCodeProvider provider) {
	// this.array = array;
	// this.hashCode = provider.hash(array);
	// }
	//
	// public boolean equals(Object o) {
	// return (o != null && o instanceof HashedObject)
	// && Arrays.equals(((HashedObject) o).array, array);
	// }
	//
	// public int hashCode() {
	// return hashCode;
	// }
	//
	// public String toString() {
	// return Arrays.toString(array) + " " + hashCode;
	// }
	// }

	protected static double expectedCol(int keys, long vals) {
		double n = keys;
		double d = vals;
		return n - d + d * Math.pow(1 - 1 / d, n);
	}

	protected static double computeChiSq(int keys, long buckets,
			TIntIntMap keyCount) {
		// makes sense only if number of buckets is small, i.e. for sure not 1L
		// << 32.
		//
		// http://www.kfki.hu/~kadlec/sw/netfilter/ct2/

		final double b = buckets;
		final double k = keys;
		final double p = k / b;

		final TIntIntMap inverse = new TIntIntHashMap();
		keyCount.forEachEntry(new TIntIntProcedure() {

			public boolean execute(int a, int b) {
				inverse.adjustOrPutValue(b, 1, 1);
				return true;
			}
		});

		final BDPointer sum = new BDPointer();

		inverse.forEachEntry(new TIntIntProcedure() {

			public boolean execute(int i, int bi) {
				double val = bi * (i - p) * (i - p) / p;
				sum.d += val;
				return true;
			}
		});

		sum.d -= b;
		sum.d /= Math.sqrt(b);

		return sum.d;
	}

	private static class BDPointer {
		public double d = 0;
	}

}
