package nl.tue.storage.impl;

import java.io.IOException;
import java.io.OutputStream;

public class SkippableOutputStream extends OutputStream {

	private int pos;
	private final byte[] array;

	public SkippableOutputStream(byte[] array, int pos) {
		this.array = array;
		this.pos = pos;

	}

	@Override
	public void write(int b) throws IOException {
		array[pos++] = (byte) b;
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		System.arraycopy(b, off, array, pos, len);
		pos += len;
	}

	@Override
	public void write(byte b[]) {
		System.arraycopy(b, 0, array, pos, b.length);
		pos += b.length;
	}

	public void skip(int n) {
		pos += n;
	}

	/**
	 * When writing to this stream, you need a lock on this object.
	 * 
	 * @return
	 */
	public Object getLock() {
		return array;
	}
}
