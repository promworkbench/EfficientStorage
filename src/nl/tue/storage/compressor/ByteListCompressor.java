package nl.tue.storage.compressor;

public class ByteListCompressor extends AbstractFixedLengthListCompressor<Byte> {

	public ByteListCompressor(int length) {
		super(length);
	}

	protected boolean isZero(Byte number) {
		return number == 0;
	}

	protected int numBytes() {
		return 1;
	}

	protected byte[] toByteArray(Byte number) {
		return new byte[] { number };
	}

	protected Byte fromByteArray(byte[] bytes) {
		return bytes[0];
	}

	protected Byte[] newEmptyArray(int length) {
		Byte[] b = new Byte[length];
		for (int i = 0; i < length; i++) {
			b[i] = 0;
		}
		return b;
	}

	@Override
	public int getMaxByteCount() {
		return length;
	}

}
