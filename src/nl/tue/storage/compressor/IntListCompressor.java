package nl.tue.storage.compressor;

import java.io.IOException;
import java.io.InputStream;

public class IntListCompressor extends
		AbstractFixedLengthListCompressor<Integer> {

	public IntListCompressor(int length) {
		super(length);
	}

	protected boolean isZero(Integer number) {
		return number == 0;
	}

	protected int numBytes() {
		return 4;
	}

	protected byte[] toByteArray(Integer i) {
		int value = i;
		return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16),
				(byte) (value >>> 8), (byte) value };

	}

	protected Integer fromByteArray(byte[] b) {
		return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8)
				+ (b[3] & 0xFF);
	}

	protected int fromStream(InputStream stream) throws IOException {
		return (stream.read() << 24) + ((stream.read() & 0xFF) << 16)
				+ ((stream.read() & 0xFF) << 8) + (stream.read() & 0xFF);
	}

	// protected byte[] toByteArray(Integer i) {
	// byte[] result = new byte[4];
	// for (int k = 0; k < 4; k++) {
	// result[3 - k] = (byte) (i >>> (k * 8));
	// }
	// return result;
	// }

	// protected Integer fromByteArray(byte[] bytes) {
	// int l = 0;
	// for (int i = 0; i < 4; i++) {
	// l <<= 8;
	// l ^= bytes[i] & 0xFF;
	// }
	//
	// return l;
	// }

	protected Integer[] newEmptyArray(int length) {
		Integer[] b = new Integer[length];
		for (int i = 0; i < length; i++) {
			b[i] = 0;
		}
		return b;
	}

	@Override
	public int getMaxByteCount() {
		return 4 * length;
	}

}
