package nl.tue.storage.compressor;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;

import java.util.Arrays;
import java.util.Collection;

public class BitMask implements Cloneable {

	private int ones;
	private final byte[] bytes;
	private final static int[] POWER = new int[] { 1, 2, 4, 8, 16, 32, 64, 128 };
	private final int numberOfBits;

	public BitMask(byte[] bytes, int ones, int numberOfBits) {
		this.bytes = bytes;
		this.ones = ones;
		this.numberOfBits = numberOfBits;
	}

	public BitMask(byte[] bytes, int numberOfBits) {
		this.bytes = bytes;
		this.numberOfBits = numberOfBits;
		int one = 0;
		int b;
		for (int i = 0; i < bytes.length; i++) {
			b = 256 + bytes[i];
			for (int j = 0; j < 8; j++) {
				one += b & 1;
				b = (b >> 1);
			}
		}
		assert one <= numberOfBits;
		this.ones = one;
	}

	public BitMask(int numberOfBits) {
		this.numberOfBits = numberOfBits;
		this.ones = 0;
		this.bytes = new byte[getNumBytes(numberOfBits)];
	}

	public boolean equals(Object o) {
		return (o != null) && (o instanceof BitMask)
				&& (((BitMask) o).getOnes() == ones)
				&& (((BitMask) o).getNumBits() == numberOfBits)
				&& Arrays.equals(((BitMask) o).getBytes(), bytes);
	}

	/**
	 * The hashcode of BitMask is defined as the hashcode of its bytes.
	 */
	public int hashCode() {
		return Arrays.hashCode(bytes);
	}

	public void set(int index, boolean value) {
		int bte = index / 8;
		int bit = index % 8;
		if (((bytes[bte] & POWER[bit]) > 0) != value) {
			// flipping a bit
			ones += value ? 1 : -1;
			bytes[bte] = (byte) (bytes[bte] ^ POWER[bit]);
		}
	}

	/**
	 * returns the number of bytes in a bitmask for a list containing precisely
	 * size elements.
	 * 
	 * @param size
	 * @return
	 */
	public static int getNumBytes(int size) {
		return ((size - 1) / 8) + 1;
	}

	/**
	 * Flag the bits at the given indices
	 * 
	 * @param indices
	 */
	public static BitMask makeBitMask(int size, int... indices) {
		byte[] bitmask = new byte[getNumBytes(size)];
		for (int i : indices) {
			int bte = i / 8;
			int bit = i % 8;
			bitmask[bte] = (byte) (bitmask[bte] | POWER[bit]);
		}
		return new BitMask(bitmask, indices.length, size);
	}

	/**
	 * Flag the bits at the given indices
	 * 
	 * @param indices
	 */
	public static BitMask makeBitMask(int size, short... indices) {
		byte[] bitmask = new byte[getNumBytes(size)];
		for (int i : indices) {
			int bte = i / 8;
			int bit = i % 8;
			bitmask[bte] = (byte) (bitmask[bte] | POWER[bit]);
		}
		return new BitMask(bitmask, indices.length, size);
	}

	/**
	 * Flag the bits at the given indices
	 * 
	 * @param indices
	 */
	public static BitMask makeBitMask(int size, TIntSet indices) {
		byte[] bitmask = new byte[getNumBytes(size)];
		int pow = 1;
		TIntIterator it = indices.iterator();
		while (it.hasNext()) {
			int i = it.next();
			int bte = i / 8;
			int bit = i % 8;
			pow = (bit == 0 ? 1 : pow * 2);
			bitmask[bte] = (byte) (bitmask[bte] | POWER[bit]);
		}
		return new BitMask(bitmask, indices.size(), size);
	}

	/**
	 * Flag the bits at the given indices
	 * 
	 * @param indices
	 */
	public static BitMask makeBitMask(int size, Collection<Integer> indices) {
		byte[] bitmask = new byte[getNumBytes(size)];
		int pow = 1;
		for (int i : indices) {
			int bte = i / 8;
			int bit = i % 8;
			pow = (bit == 0 ? 1 : pow * 2);
			bitmask[bte] = (byte) (bitmask[bte] | POWER[bit]);
		}
		return new BitMask(bitmask, indices.size(), size);
	}

	/**
	 * return the indices flagged by this bitmask
	 * 
	 * @return
	 */
	public static int[] getIndices(BitMask bitmask) {

		int[] result = new int[bitmask.getOnes()];
		int s = 0;
		for (int i = 0; i < bitmask.getBytes().length; i++) {
			byte b = bitmask.getBytes()[i];
			byte bit = 0;
			for (int j = 1; j <= 128; j *= 2) {
				if ((b & j) > 0) {
					result[s] = i * 8 + bit;
					s++;
				}
				bit++;
			}
		}

		return result;
	}

	public TBooleanIterator iterator() {
		return new TBooleanIterator() {

			int next = 0;

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"Cannot remove bits from a bitmask");
			}

			@Override
			public boolean hasNext() {
				return next < numberOfBits;
			}

			@Override
			public boolean next() {
				int bte = next / 8;
				int bit = next % 8;
				next++;
				return (bytes[bte] & POWER[bit]) > 0;
			}
		};
	}

	public int getOnes() {
		return ones;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getNumBits() {
		return numberOfBits;
	}

	public boolean[] toBooleanArray() {
		boolean[] result = new boolean[numberOfBits];
		int i = 0;
		TBooleanIterator it = iterator();
		while (it.hasNext()) {
			result[i++] = it.next();
		}
		return result;
	}

	public BitMask clone() {
		return new BitMask(Arrays.copyOf(bytes, bytes.length), ones,
				numberOfBits);
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("[");
		TBooleanIterator it = iterator();
		while (it.hasNext()) {
			s.append(it.next() ? "T" : "F");
			s.append(it.hasNext() ? "," : "");
		}
		s.append("]");
		return s.toString();
	}

}
