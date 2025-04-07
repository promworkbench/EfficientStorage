package nl.tue.astar.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.Deflater;
import nl.tue.storage.Inflater;
import nl.tue.storage.compressor.BitMask;

public abstract class AbstractCompressor<H> implements Deflater<H>, Inflater<H> {

	private final static IOException ex = new IOException();

	private byte[] writeBuffer = new byte[8];
	/*
	 * HV: For sake of debugging.
	 */
//	private static Set<Short> _lengths = new HashSet<Short>();
//	private static Set<Integer> _indices = new HashSet<Integer>();
//	private static Set<Short> _values = new HashSet<Short>();

	protected BitMask readMask(InputStream stream, int length, int numBytes)
			throws IOException {
		byte[] mask = new byte[numBytes];
		stream.read(mask);
		return new BitMask(mask, length);
	}

	protected ShortShortMultiset inflateContent(InputStream stream, int[] ids,
			short length) throws IOException {
		ShortShortMultiset result = new ShortShortMultiset(length);

//		if (!_lengths.contains(length)) {
//			_lengths.add(length);
//			System.out.println("[AbstractCompressor] new length: " + length);
//		}
		for (int i : ids) {
//			if (!_indices.contains(i)) {
//				_indices.add(i);
//				System.err.println("[AbstractCompressor] new index: " + i + " (length = " + length + ")");
//			}
			short val = readShortFromStream(stream);
//			if (!_values.contains(val)) {
//				_values.add(val);
//				System.err.println("[AbstractCompressor] new value: " + val + " (index = " + i + ", length = " + length + ")");
//			}
			result.put((short) i, val);
		}

		return result;
	}

	protected short[] inflateContentToArray(InputStream stream, int[] ids,
			short length) throws IOException {
		short[] result = new short[length];

		for (int i : ids) {
			short val = readShortFromStream(stream);
			result[i] = val;
		}

		return result;
	}

	protected void writeShortToByteArray(OutputStream stream, short i)
			throws IOException {
		// stream.write(i >>> 24);
		// stream.write(i >>> 16);
		stream.write(i >>> 8);
		stream.write(i);

	}

	protected void writeBooleanToByteArray(OutputStream stream, boolean b)
			throws IOException {
		// stream.write(i >>> 24);
		// stream.write(i >>> 16);
		stream.write(b ? 1 : 0);

	}

	protected boolean checkShortOnByteArray(InputStream stream, short i)
			throws IOException {
		return i == readShortFromStream(stream);

	}

	protected short readShortFromStream(InputStream stream) throws IOException {
		return (short) ((((byte) stream.read() & 0xFF) << 8) + (((byte) stream
				.read()) & 0xFF));
	}

	/**
	 * Write 4 bytes of an integer
	 * 
	 * @param stream
	 * @param next
	 * @throws IOException
	 */
	protected void writeIntToByteArray(OutputStream out, int v)
			throws IOException {
		out.write((v >>> 24) & 0xFF);
		out.write((v >>> 16) & 0xFF);
		out.write((v >>> 8) & 0xFF);
		out.write(v & 0xff);
	}

	protected void writeLongToByteArray(OutputStream stream, long v)
			throws IOException {
		writeBuffer[0] = (byte) (v >>> 56);
		writeBuffer[1] = (byte) (v >>> 48);
		writeBuffer[2] = (byte) (v >>> 40);
		writeBuffer[3] = (byte) (v >>> 32);
		writeBuffer[4] = (byte) (v >>> 24);
		writeBuffer[5] = (byte) (v >>> 16);
		writeBuffer[6] = (byte) (v >>> 8);
		writeBuffer[7] = (byte) (v);
		stream.write(writeBuffer, 0, 8);
	}

	/**
	 * Write 4 bytes of an integer
	 * 
	 * @param stream
	 * @param next
	 * @throws IOException
	 */
	protected void writeDoubleToByteArray(OutputStream stream, double v)
			throws IOException {
		writeLongToByteArray(stream, Double.doubleToLongBits(v));
	}

	protected boolean checkIntOnByteArray(InputStream stream, int i)
			throws IOException {
		return i == readShortFromStream(stream);

	}

	/**
	 * Return the first eight bytes as an long
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected double readDoubleFromStream(InputStream stream)
			throws IOException {
		return Double.longBitsToDouble(readLongFromStream(stream));
	}

	/**
	 * Return the first eight bytes as an long
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected long readLongFromStream(InputStream stream) throws IOException {
		long l = (((long) stream.read() << 56) + ((long) stream.read() << 48)
				+ ((long) stream.read() << 40) + ((long) stream.read() << 32)
				+ ((long) stream.read() << 24) + (stream.read() << 16)
				+ (stream.read() << 8) + stream.read());
		return l;
	}

	/**
	 * Return the first four bytes as an integer
	 * 
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	protected int readIntFromStream(InputStream stream) throws IOException {
		return ((stream.read() << 24) + (stream.read() << 16)
				+ (stream.read() << 8) + (stream.read() << 0));
	}

	protected boolean readBooleanFromStream(InputStream stream)
			throws IOException {
		return stream.read() != 0;
	}

	protected void deflate(ShortShortMultiset object, OutputStream stream,
			int length) throws IOException {
		stream.write(makeBitMask(length, object).getBytes());
		for (short i = 0; i < length; i++) {
			short val = object.get(i);
			if (val > 0) {
				writeShortToByteArray(stream, val);
			}
		}
	}

	protected void deflate(short[] object, OutputStream stream, int length)
			throws IOException {
		stream.write(makeBitMask(length, object).getBytes());
		for (short i = 0; i < length; i++) {
			short val = object[i];
			if (val > 0) {
				writeShortToByteArray(stream, val);
			}
		}
	}

	protected boolean deflateWithCheck(ShortShortMultiset object, int length,
			InputStream input) throws IOException {
		byte[] mask = makeBitMask(length, object).getBytes();
		// check for equality of mask with the mask in the stream
		for (int i = 0; i < mask.length; i++) {
			int r = input.read();
			if (r < 0 || ((byte) r != mask[i])) {
				return false;
			}
		}
		// check for contents
		for (short i = 0; i < length; i++) {
			short val = object.get(i);
			if (val > 0) {
				if (!checkShortOnByteArray(input, val)) {
					return false;
				}
			}
		}
		return true;
	}

	protected final static int[] POWER = new int[] { 1, 2, 4, 8, 16, 32, 64,
			128 };

	protected static BitMask makeBitMask(int size, ShortShortMultiset indices) {
		byte[] bitmask = new byte[BitMask.getNumBytes(size)];
		// iterating over all elements if possible indices is faster than
		// first getting the relevant keys.
		for (short i = 0; i < indices.getLength(); i++) {
			if (indices.get(i) > 0) {
				int bte = i / 8;
				int bit = i % 8;
				bitmask[bte] = (byte) (bitmask[bte] | POWER[bit]);
			}
		}
		return new BitMask(bitmask, indices.size(), size);
	}

	protected static BitMask makeBitMask(int size, short[] values) {
		byte[] bitmask = new byte[BitMask.getNumBytes(size)];
		// iterating over all elements if possible indices is faster than
		// first getting the relevant keys.
		int cnt = 0;
		for (short i = 0; i < values.length; i++) {
			if (values[i] > 0) {
				cnt++;
				int bte = i / 8;
				int bit = i % 8;
				bitmask[bte] = (byte) (bitmask[bte] | POWER[bit]);
			}
		}
		return new BitMask(bitmask, cnt, size);
	}

	protected static BitMask makeShortListBitMask(int size, short[] marking) {
		byte[] bitmask = new byte[BitMask.getNumBytes(size)];
		// iterating over all elements if possible indices is faster than
		// first getting the relevant keys.
		for (short j = 0; j < marking.length; j++) {
			if (marking[j] > 0) {
				int bte = j / 8;
				int bit = j % 8;
				bitmask[bte] = (byte) (bitmask[bte] | POWER[bit]);
			}
		}
		return new BitMask(bitmask, marking.length, size);
	}

	protected boolean equalsDeflating(H head, CompressedStore<?> store, long l) {
		final InputStream stream = store.getStreamForObject(l);
		OutputStream out = new OutputStream() {
			public void write(int i) throws IOException {
				if ((byte) stream.read() != (byte) i) {
					throw ex;
				}
			}

		};
		try {
			deflate(head, out);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * writes a byte[] of vars into a byte[] toWrite, where each element of vars
	 * is encoded using precisely bitsPerVar bits. If bitsPerVar == 8, the
	 * result is identical to the input
	 * 
	 * @param toWrite
	 *            the byte[] to write to. The size should be at least
	 *            Math.ceil(vars.length/8*bitsPerVar)
	 * @param vars
	 *            the byte[] to encode.
	 * @param bitsPerVar
	 *            the number of bits per element of var. For all i: vars[i] < (1
	 *            << bitsPerVar)
	 */
	public static void writeTo(byte[] toWrite, byte[] vars, int bitsPerVar) {
		assert toWrite.length >= Math.ceil(vars.length / 8 * bitsPerVar);
		int bit = 7;
		int bte = -1;
		for (int i = 0; i < vars.length; i++) {
			// write bits
			int var = vars[i] & 0xFF;
			assert var < 1 << bitsPerVar;
			for (int bt = 0; bt < bitsPerVar; bt++) {
				bte += (bit == 7 ? 1 : 0);
				toWrite[bte] |= (1 & var) << bit;
				var = var >> 1;
				bit = (bit - 1) & 7;
			}
		}

	}

	/**
	 * reads a byte[] of vars from a byte[] toRead, where each element of vars
	 * is encoded using precisely bitsPerVar bits.
	 * 
	 * @param toRead
	 *            the byte[] to read from. The size should be at least
	 *            Math.ceil(vars.length/8*bitsPerVar)
	 * @param vars
	 *            the byte[] to fill.
	 * @param bitsPerVar
	 *            the number of bits per element of var. For all i: vars[i] < (1
	 *            << bitsPerVar)
	 */
	public static void readInto(byte[] toRead, byte[] vars, int bitsPerVar) {
		int bit = 7;
		int bte = -1;
		for (int i = 0; i < vars.length; i++) {
			// read bits
			int var = 0;
			for (int bt = 0; bt < bitsPerVar; bt++) {
				bte += (bit == 7 ? 1 : 0);
				int read = toRead[bte] & 0xFF;
				var |= ((read >> bit) & 1) << bt;
				bit = (bit - 1) & 7;
			}
			vars[i] = (byte) var;
		}

	}

	public static int getBitsPerVar(byte[] vars) {
		int bitsPerVar = 0;
		for (int v = 0; v < vars.length; v++) {
			while (1 << bitsPerVar < (vars[v] & 0xFF)) {
				bitsPerVar++;
			}
		}
		return bitsPerVar;
	}

	public static int getBitsPerVar(short[] vars) {
		int bitsPerVar = 0;
		for (int v = 0; v < vars.length; v++) {
			while (1 << bitsPerVar < (vars[v] & 0xFFFF)) {
				bitsPerVar++;
			}
		}
		return bitsPerVar;
	}

	/**
	 * writes a byte[] of vars into a byte[] toWrite, where each element of vars
	 * is encoded using precisely bitsPerVar bits. If bitsPerVar == 8, the
	 * result is identical to the input
	 * 
	 * @param toWrite
	 *            the byte[] to write to. The size should be at least
	 *            Math.ceil(vars.length/8*bitsPerVar)
	 * @param vars
	 *            the byte[] to encode.
	 * @param bitsPerVar
	 *            the number of bits per element of var. For all i: vars[i] < (1
	 *            << bitsPerVar)
	 */
	public static void writeTo(byte[] toWrite, short[] vars, int bitsPerVar) {
		assert toWrite.length >= Math.ceil(vars.length / 8 * bitsPerVar);
		int bit = 7;
		int bte = -1;
		for (int i = 0; i < vars.length; i++) {
			// write bits
			int var = vars[i] & 0xFFFF;
			assert var < 1 << bitsPerVar;
			for (int bt = 0; bt < bitsPerVar; bt++) {
				bte += (bit == 7 ? 1 : 0);
				toWrite[bte] |= (1 & var) << bit;
				var = var >> 1;
				bit = (bit - 1) & 7;
			}
		}

	}

	/**
	 * reads a byte[] of vars from a byte[] toRead, where each element of vars
	 * is encoded using precisely bitsPerVar bits.
	 * 
	 * @param toRead
	 *            the byte[] to read from. The size should be at least
	 *            Math.ceil(vars.length/8*bitsPerVar)
	 * @param vars
	 *            the byte[] to fill.
	 * @param bitsPerVar
	 *            the number of bits per element of var. For all i: vars[i] < (1
	 *            << bitsPerVar)
	 */
	public static void readInto(byte[] toRead, short[] vars, int bitsPerVar) {
		int bit = 7;
		int bte = -1;
		for (int i = 0; i < vars.length; i++) {
			// read bits
			int var = 0;
			for (int bt = 0; bt < bitsPerVar; bt++) {
				bte += (bit == 7 ? 1 : 0);
				int read = toRead[bte] & 0xFF;
				var |= ((read >> bit) & 1) << bt;
				bit = (bit - 1) & 7;
			}
			vars[i] = (short) var;
		}

	}

}
