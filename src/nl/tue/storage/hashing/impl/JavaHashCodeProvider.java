package nl.tue.storage.hashing.impl;


public class JavaHashCodeProvider extends AbstractHashCodeProvider {

	private final int par;

	public JavaHashCodeProvider(int par) {
		super(1, "JAVA" + par);
		this.par = par;
	}

	public JavaHashCodeProvider() {
		this(31);
	}

	protected JavaHashCodeProvider(int init, int par, String name) {
		super(init, name);
		this.par = par;
	}

	@Override
	protected int hashInternal(int oldHash, final short[] array) {
		for (short s : array) {
			oldHash = par * oldHash + s;
		}
		return oldHash;
	}

	@Override
	protected int hashInternal(int oldHash, final int[] array) {
		for (int s : array) {
			oldHash = par * oldHash + s;
		}
		return oldHash;
	}
}
