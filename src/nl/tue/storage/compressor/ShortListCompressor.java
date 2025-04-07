package nl.tue.storage.compressor;

public class ShortListCompressor extends
		AbstractFixedLengthListCompressor<Short> {

	public ShortListCompressor(int length) {
		super(length);
	}

	protected boolean isZero(Short number) {
		return number == 0;
	}

	protected int numBytes() {
		return 2;
	}

	protected byte[] toByteArray(Short i) {
		byte[] result = new byte[2];
		for (int k = 0; k < 2; k++) {
			result[1 - k] = (byte) (i >>> (k * 8));
		}
		return result;
	}

	protected Short fromByteArray(byte[] bytes) {
		short l = 0;
		for (int i = 0; i < 2; i++) {
			l <<= 8;
			l ^= bytes[i] & 0xFF;
		}

		return l;
	}

	protected Short[] newEmptyArray(int length) {
		Short[] b = new Short[length];
		for (int i = 0; i < length; i++) {
			b[i] = 0;
		}
		return b;
	}

	@Override
	public int getMaxByteCount() {
		return 2 * length;
	}

}
