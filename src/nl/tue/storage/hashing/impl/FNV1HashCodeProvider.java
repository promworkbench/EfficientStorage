package nl.tue.storage.hashing.impl;


public class FNV1HashCodeProvider extends AbstractHashCodeProvider {

	protected final static int FNV_32_PRIME = 0x01000193;
	protected final static int FNV1INIT = 0x811c9dc5;

	public FNV1HashCodeProvider() {
		super(FNV1INIT, "FNV1");
	}

	@Override
	protected int hashInternal(int hval, final short[] array) {
		for (short s : array) {
			int o = 8;
			for (byte b = (byte) ((s >>> o) & 0xff); o > 0; o -= 8) {
				/* multiply by the 32 bit FNV magic prime mod 2^32 */
				hval *= FNV_32_PRIME;
				/* xor the bottom with the first octet */
				hval ^= b;
			}
			hval *= FNV_32_PRIME;
			hval ^= (s & 0xff);
		}
		return hval;
	}

	@Override
	protected int hashInternal(int hval, final int[] array) {
		for (int s : array) {
			int o = 24;
			for (byte b = (byte) ((s >>> o) & 0xff); o > 0; o -= 8) {
				/* multiply by the 32 bit FNV magic prime mod 2^32 */
				hval *= FNV_32_PRIME;
				/* xor the bottom with the first octet */
				hval ^= b;
			}
			hval *= FNV_32_PRIME;
			hval ^= (s & 0xff);
		}
		return hval;
	}
}
