package nl.tue.storage.hashing.impl;

import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.hashing.IncrementalHashCodeProvider;

/**
 * The AdHashHashCodeProvider is an incremental hash function, loosely based on
 * the MurMur3 hash function.
 * 
 * The idea comes from "M. Bellare, and D. Micciancio, "A new paradigm for
 * collision-free hashing: Incrementality at reduced cost, Advances in
 * Cryptology - EUROCRYPT'97, Lecture Notes in Computer Science, 1233, pp.163-
 * 192, Springer-Verlag, 1997."
 * 
 * 
 * @author bfvdonge
 * 
 */
public class AdHashHashCodeProvider implements IncrementalHashCodeProvider {

	public final static int INIT = 0xB0F57EE3;
	public final static int C1 = 0xcc9e2d51;
	public final static int C2 = 0x1b873593;

	public AdHashHashCodeProvider() {
	}

	public String toString() {
		return "AdHASH";
	}

	@Override
	public int hash(ShortShortMultiset... sets) {
		long hash = 0;
		int i = 0;
		for (ShortShortMultiset set : sets) {
			for (short s : set.getInternalValues()) {
				hash += h(++i, s);
				hash &= 0xffffffffL;
			}
		}
		return (int) hash;
	}

	@Override
	public int hash(short[]... sets) {
		long hash = 0;
		int i = 0;
		for (short[] set : sets) {
			for (short s : set) {
				hash += h(++i, s);
				hash &= 0xffffffffL;
			}
		}
		return (int) hash;
	}

	protected int h(int idx, short s) {
		int hash = INIT;

		int k1 = idx;
		k1 *= C1;
		k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
		k1 *= C2;
		hash ^= k1;
		hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
		hash = hash * 5 + 0xe6546b64;

		k1 = (s & 0xffff);
		k1 *= C1;
		k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
		k1 *= C2;

		return hash ^ k1;

	}

	protected int h(int idx, int s) {
		int hash = INIT;

		int k1 = idx;
		k1 *= C1;
		k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
		k1 *= C2;
		hash ^= k1;
		hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);
		hash = hash * 5 + 0xe6546b64;

		k1 = s;
		k1 *= C1;
		k1 = (k1 << 15) | (k1 >>> 17); // ROTL32(k1,15);
		k1 *= C2;
		hash ^= k1;
		hash = (hash << 13) | (hash >>> 19); // ROTL32(h1,13);

		return hash * 5 + 0xe6546b64;
	}

	// private int h(short idx, short val) {
	// int hash = 1;
	// hash += idx & 0xff;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += (idx >>> 8);
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += val & 0xff;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += val >>> 8;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// return hash;
	// }
	//
	// private int h(short idx, int val) {
	// int hash = 1;
	// hash += idx & 0xff;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += (idx >>> 8);
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += val & 0xff;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += (val >>> 8) & 0xff;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += (val >>> 16) & 0xff;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// hash += (val >>> 24) & 0xff;
	// hash += (hash << 10);
	// hash ^= (hash >>> 6);
	//
	// return hash;
	// }

	@Override
	public int hash(int[]... sets) {
		long hash = 0;
		int i = 0;
		for (int[] set : sets) {
			for (int s : set) {
				hash += h(++i, s);
				hash &= 0xffffffffL;
			}
		}
		return (int) hash;
	}

	@Override
	public int updateHash(int oldHash, int idx, short oldVal, short newVal) {
		long hash = oldHash;// & 0xffffffff;
		hash -= h(++idx, oldVal);
		//hash &= 0xffffffff;
		hash += h(idx, newVal);
		return (int) (hash & 0xffffffffL);
	}

	@Override
	public int updateHash(int oldHash, int idx, int oldVal, int newVal) {
		long hash = oldHash;// & 0xffffffff;
		hash -= h(++idx, oldVal);
		//hash &= 0xffffffff;
		hash += h(idx, newVal);
		return (int) (hash & 0xffffffffL);
	}

}
