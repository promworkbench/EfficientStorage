package nl.tue.storage.compressor;

public class LongListCompressor extends AbstractFixedLengthListCompressor<Long> {

	public LongListCompressor(int length) {
		super(length);
	}

	protected boolean isZero(Long number) {
		return number == 0;
	}

	protected int numBytes() {
		return 8;
	}

	protected byte[] toByteArray(Long i) {
		byte[] result = new byte[8];
		for (int k = 0; k < 8; k++) {
			result[7 - k] = (byte) (i >>> (k * 8));
		}
		return result;
	}

	protected Long fromByteArray(byte[] bytes) {
		long l = 0;
		for (int i = 0; i < 8; i++) {
			l <<= 8;
			l ^= bytes[i] & 0xFF;
		}

		return l;
	}

	protected Long[] newEmptyArray(int length) {
		Long[] b = new Long[length];
		for (int i = 0; i < length; i++) {
			b[i] = 0l;
		}
		return b;
	}

	@Override
	public int getMaxByteCount() {
		return 8 * length;
	}

}
