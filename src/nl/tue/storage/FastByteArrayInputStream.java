package nl.tue.storage;

import java.io.InputStream;

/**
 * ByteArrayInputStream implementation that does not synchronize methods, nor
 * does it check for the end of stream. If the end of stream is read, exceptions
 * may be thrown.
 */
public class FastByteArrayInputStream extends InputStream {
	/**
	 * Our byte buffer
	 */
	protected final byte[] buf;

	/**
	 * Number of bytes that we can read from the buffer
	 */
	protected int count = 0;

	/**
	 * Number of bytes that have been read from the buffer
	 */
	protected int pos = 0;

	public FastByteArrayInputStream(byte[] buf, int pos, int count) {
		this.buf = buf;
		this.pos = pos;
		this.count = pos + count;
	}

	public final int available() {
		return count - pos;
	}

	public final int read() {
		assert pos < count;
		return buf[pos++] & 0xff;
	}

	public final int read(byte[] b, int off, int len) {
		assert (pos + len <= count);
		System.arraycopy(buf, pos, b, off, len);
		pos += len;
		return len;
	}

	public final long skip(long n) {
		assert (pos + n) <= count;
		pos += n;
		return n;
	}

	/**
	 * when reading from this stream, you need a lock on this object.
	 * 
	 * @return
	 */
	public Object getLock() {
		return buf;
	}

}