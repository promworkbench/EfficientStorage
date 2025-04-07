package nl.tue.storage.hashing.impl;

import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.hashing.HashCodeProvider;

public class JenkingsHashCodeProvider implements HashCodeProvider {

	// Hash function taken from http://www.burtleburtle.net/bob/c/lookup3.c

	private int a;
	private int b;
	private int c;

	public JenkingsHashCodeProvider() {

	}

	public String toString() {
		return "Jenkings";
	}

	@Override
	public synchronized int hash(final ShortShortMultiset... sets) {
		int hash = 0;
		for (ShortShortMultiset set : sets) {
			hash = hashInternal(set.getInternalValues(), hash);
		}
		return hash;
	}

	@Override
	public synchronized int hash(final short[]... sets) {
		int hash = 0;
		for (short[] set : sets) {
			hash = hashInternal(set, hash);
		}
		return hash;
	}

	@Override
	public synchronized int hash(final int[]... sets) {
		int hash = 0;
		for (int[] set : sets) {
			hash = hashInternal(set, hash);
		}
		return hash;
	}

	protected synchronized int hashInternal(final short[] marking,
			final int hash) {
		int len = marking.length;

		a = b = c = 0xdeadbeef + 2 * len + hash;

		short i = 0;
		while (len > 6) {
			a += (marking[i++] << 16) | (marking[i++] & 0xffff);
			b += (marking[i++] << 16) | (marking[i++] & 0xffff);
			c += (marking[i++] << 16) | (marking[i++] & 0xffff);
			len -= 6;
			mix();
		}
		switch (len) {
		case 6:
			c += marking[i + 5];
			//$FALL-THROUGH$
		case 5:
			c += marking[i + 4] << 16;
			//$FALL-THROUGH$
		case 4:
			b += marking[i + 3];
			//$FALL-THROUGH$
		case 3:
			b += marking[i + 2] << 16;
			//$FALL-THROUGH$
		case 2:
			a += marking[i + 1];
			//$FALL-THROUGH$
		case 1:
			a += marking[i] << 16;
			finalHash();
			//$FALL-THROUGH$
		case 0:
		}
		return c;

	}

	protected synchronized void finalHash() {
		c ^= b;
		c -= rot(b, 14);
		a ^= c;
		a -= rot(c, 11);
		b ^= a;
		b -= rot(a, 25);
		c ^= b;
		c -= rot(b, 16);
		a ^= c;
		a -= rot(c, 4);
		b ^= a;
		b -= rot(a, 14);
		c ^= b;
		c -= rot(b, 24);
	}

	protected synchronized void mix() {
		a -= c;
		a ^= rot(c, 4);
		c += b;
		b -= a;
		b ^= rot(a, 6);
		a += c;
		c -= b;
		c ^= rot(b, 8);
		b += a;
		a -= c;
		a ^= rot(c, 16);
		c += b;
		b -= a;
		b ^= rot(a, 19);
		a += c;
		c -= b;
		c ^= rot(b, 4);
		b += a;
	}

	protected synchronized int rot(final int x, final int k) {
		return (x << k) | (x >>> (32 - k));
	}

	protected synchronized int hashInternal(final int[] marking, final int hash) {
		int len = marking.length;

		a = b = c = 0xdeadbeef + len + hash;

		int i = 0;
		while (len > 3) {
			a += marking[i++];
			b += marking[i++];
			c += marking[i++];
			len -= 3;
			mix();
		}
		switch (len) {
		case 3:
			c += marking[i + 2];
			//$FALL-THROUGH$
		case 2:
			b += marking[i + 1];
			//$FALL-THROUGH$
		case 1:
			a += marking[i];
			finalHash();
			//$FALL-THROUGH$
		case 0:
		}
		return c;

	}

}