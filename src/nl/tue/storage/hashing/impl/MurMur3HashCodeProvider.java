package nl.tue.storage.hashing.impl;

import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.hashing.HashCodeProvider;

public class MurMur3HashCodeProvider implements HashCodeProvider {

	public final static int INIT = 0xB0F57EE3;
	public final static int C1 = 0xcc9e2d51;
	public final static int C2 = 0x1b873593;

	public MurMur3HashCodeProvider() {
		// implementation adopted from
		// https://github.com/yonik/java_util/blob/master/src/util/hash/MurmurHash3.java
	}

	public String toString() {
		return "MurMur3";
	}

	public int hash(final ShortShortMultiset... sets) {
		int hash = INIT;
		for (ShortShortMultiset set : sets) {
			hash = hashInternal(set.getInternalValues(), hash);
		}
		return hash;
	}

	public int hash(final short[]... sets) {
		int hash = INIT;
		for (short[] set : sets) {
			hash = hashInternal(set, hash);
		}
		return hash;
	}

	public int hash(final int[]... sets) {
		int hash = INIT;
		for (int[] set : sets) {
			hash = hashInternal(set, hash);
		}
		return hash;
	}

	/** Returns the MurmurHash3_x86_32 hash. */
	protected int hashInternal(final short[] data, int hash) {

		final int len = data.length;

		int k1;
		for (int i = 0; i < len - 1; i += 2) {
			// little endian load order
			k1 = (data[i] & 0xffff) | ((data[i + 1] & 0xffff) << 16);
			k1 *= C1;

			k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
			k1 *= C2;

			hash ^= k1;

			hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
			hash = hash * 5 + 0xe6546b64;

		}

		// tail
		if ((len & 1) == 1) {
			k1 = (data[len - 1] & 0xffff);
			k1 *= C1;
			k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
			k1 *= C2;
			hash ^= k1;
		}

		// finalization
		hash ^= len;

		// fmix(h1);
		hash ^= hash >>> 16;
		hash *= 0x85ebca6b;
		hash ^= hash >>> 13;
		hash *= 0xc2b2ae35;
		hash ^= hash >>> 16;

		return hash;
	}

	/** Returns the MurmurHash3_x86_32 hash. */
	protected int hashInternal(final int[] data, int hash) {

		final int len = data.length;

		int k1;
		for (int i = 0; i < len - 1; i++) {
			// little endian load order
			k1 = data[i];
			k1 *= C1;

			k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
			k1 *= C2;

			hash ^= k1;

			hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
			hash = hash * 5 + 0xe6546b64;

		}

		// tail
		k1 = data[len - 1];
		k1 *= C1;
		k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
		k1 *= C2;
		hash ^= k1;

		// finalization
		hash ^= len;

		// fmix(h1);
		hash ^= hash >>> 16;
		hash *= 0x85ebca6b;
		hash ^= hash >>> 13;
		hash *= 0xc2b2ae35;
		hash ^= hash >>> 16;

		return hash;
	}

}
