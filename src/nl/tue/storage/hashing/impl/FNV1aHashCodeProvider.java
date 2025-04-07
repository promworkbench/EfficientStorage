package nl.tue.storage.hashing.impl;

public class FNV1aHashCodeProvider extends AbstractHashCodeProvider {

	protected final static int FNV_32_PRIME = 0x01000193;
	protected final static int FNV1INIT = 0x811c9dc5;

	public FNV1aHashCodeProvider() {
		super(FNV1INIT, "FNV1a");
	}

	@Override
	protected int hashInternal(int hval, final short[] array) {
		for (short s : array) {
			int o = 8;
			for (int b = ((s >>> o) & 0xff); o > 0; o -= 8) {
				/* xor the bottom with the first octet */
				hval ^= b;
				/* multiply by the 32 bit FNV magic prime mod 2^32 */
				hval *= FNV_32_PRIME;
			}
			/* xor the bottom with the first octet */
			hval ^= (s & 0xff);
			/* multiply by the 32 bit FNV magic prime mod 2^32 */
			hval *= FNV_32_PRIME;
		}
		return hval;
	}

	@Override
	protected int hashInternal(int hval, final int[] array) {
		for (int s : array) {
			int o = 24;
			for (int b = ((s >>> o) & 0xff); o > 0; o -= 8) {
				/* xor the bottom with the first octet */
				hval ^= b;
				/* multiply by the 32 bit FNV magic prime mod 2^32 */
				hval *= FNV_32_PRIME;
			}
			/* xor the bottom with the first octet */
			hval ^= (s & 0xff);
			/* multiply by the 32 bit FNV magic prime mod 2^32 */
			hval *= FNV_32_PRIME;
		}
		return hval;
	}

}
