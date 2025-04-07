package nl.tue.storage;

import java.io.OutputStream;

/**
 * ByteArrayOutputStream implementation that doesn't synchronize methods and
 * doesn't copy the data on toByteArray().
 * 
 * Furthermore, no capacity changes are allowed. Once the initial capacity it
 * set, it is assumed final!
 */
public class FastByteArrayOutputStream extends OutputStream {
	/**
	 * Buffer and size
	 */
	protected byte[] buf;
	protected int size = 0;

	/**
	 * Constructs a stream with buffer capacity size 5K
	 */
	public FastByteArrayOutputStream() {
		this(512);
	}

	/**
	 * Constructs a stream with the given initial size
	 */
	public FastByteArrayOutputStream(int initSize) {
		this.size = 0;
		this.buf = new byte[initSize];
	}

	public int getSize() {
		return size;
	}

	/**
	 * Returns the byte array containing the written data. Note that this array
	 * will almost always be larger than the amount of data actually written.
	 */
	public byte[] getByteArray() {
		return buf;
	}

	public void write(byte b[]) {
		System.arraycopy(b, 0, buf, size, b.length);
		size += b.length;
	}

	public void write(byte b[], int off, int len) {
		System.arraycopy(b, off, buf, size, len);
		size += len;
	}

	public void write(int b) {
		buf[size++] = (byte) b;
	}

	public void reset() {
		size = 0;
	}

}