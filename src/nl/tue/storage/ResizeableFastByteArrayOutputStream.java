package nl.tue.storage;

/**
 * ByteArrayOutputStream implementation that doesn't synchronize methods and
 * doesn't copy the data on toByteArray().
 * 
 * Furthermore, no capacity changes are allowed. Once the initial capacity it
 * set, it is assumed final!
 */
public class ResizeableFastByteArrayOutputStream extends
		FastByteArrayOutputStream {

	/**
	 * Constructs a stream with the given initial size
	 */
	public ResizeableFastByteArrayOutputStream(int initSize) {
		super(initSize);
	}

	/**
	 * Ensures that we have a large enough buffer for the given size.
	 */
	private void verifyBufferSize(int sz) {
		if (sz > buf.length) {
			byte[] old = buf;
			buf = new byte[Math.max(sz, 2 * buf.length)];
			System.arraycopy(old, 0, buf, 0, old.length);
			old = null;
		}
	}

	public void write(byte b[]) {
		verifyBufferSize(size + b.length);
		System.arraycopy(b, 0, buf, size, b.length);
		size += b.length;
	}

	public void write(byte b[], int off, int len) {
		verifyBufferSize(size + len);
		System.arraycopy(b, off, buf, size, len);
		size += len;
	}

	public void write(int b) {
		verifyBufferSize(size + 1);
		buf[size++] = (byte) b;
	}

}