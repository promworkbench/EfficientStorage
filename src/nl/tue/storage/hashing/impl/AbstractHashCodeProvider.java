package nl.tue.storage.hashing.impl;

import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.hashing.HashCodeProvider;

public abstract class AbstractHashCodeProvider implements HashCodeProvider {

	protected final int init;
	protected final String name;

	protected AbstractHashCodeProvider(int init, String name) {
		this.init = init;
		this.name = name;
	}

	public String toString() {
		return name;
	}

	@Override
	public int hash(final ShortShortMultiset... sets) {
		int hash = init;
		for (ShortShortMultiset set : sets) {
			hash = hashInternal(hash, set.getInternalValues());
		}
		return hash;
	}

	@Override
	public int hash(final short[]... sets) {
		int hash = init;
		for (short[] set : sets) {
			hash = hashInternal(hash, set);
		}
		return hash;
	}

	@Override
	public int hash(final int[]... sets) {
		int hash = init;
		for (int[] set : sets) {
			hash = hashInternal(hash, set);
		}
		return hash;
	}

	protected abstract int hashInternal(int oldHash, final short[] array);

	protected abstract int hashInternal(int oldHash, final int[] array);

}
