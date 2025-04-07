package nl.tue.storage.hashing.impl;

public class OneAtATimeHashCodeProvider extends AbstractHashCodeProvider {

	public OneAtATimeHashCodeProvider() {
		super(1, "OAT");
	}

	@Override
	protected int hashInternal(int hash, final short[] array) {
		for (short s : array) {
			hash += s & 0xff;
			hash += (hash << 10);
			hash ^= (hash >>> 6);

			hash += (s >>> 8) & 0xff;
			hash += (hash << 10);
			hash ^= (hash >>> 6);
		}
		return hash;
	}

	@Override
	protected int hashInternal(int hash, final int[] array) {
		for (int s : array) {
			hash += s & 0xff;
			hash += (hash << 10);
			hash ^= (hash >>> 6);

			hash += (s >>> 8) & 0xff;
			hash += (hash << 10);
			hash ^= (hash >>> 6);

			hash += (s >>> 16) & 0xff;
			hash += (hash << 10);
			hash ^= (hash >>> 6);

			hash += s >>> 24;
			hash += (hash << 10);
			hash ^= (hash >>> 6);
		}
		return hash;
	}

}
