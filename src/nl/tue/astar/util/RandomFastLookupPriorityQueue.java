package nl.tue.astar.util;

import java.util.Random;

import nl.tue.astar.Record;

public class RandomFastLookupPriorityQueue extends
		AbstractFastLookupPriorityQueue {

	private final double probability;

	/**
	 * Creates a {@code PriorityQueue} with the specified initial capacity that
	 * orders its elements according to the specified comparator.
	 * 
	 * @param initialCapacity
	 *            the initial capacity for this priority queue
	 * @param comparator
	 *            the comparator that will be used to order this priority queue.
	 *            If {@code null}, the {@linkplain Comparable natural ordering}
	 *            of the elements will be used.
	 * @throws IllegalArgumentException
	 *             if {@code initialCapacity} is less than 1
	 */

	public RandomFastLookupPriorityQueue(int initialCapacity, double probability) {
		super(initialCapacity);
		this.probability = probability;
	}

	public RandomFastLookupPriorityQueue(int initialCapacity, int maxCost,
			double probability) {
		super(initialCapacity, maxCost);
		this.probability = probability;
	}

	private Random r = new Random();

	@Override
	public boolean isBetter(Record r1, Record r2) {
		// retrieve stored cost
		double c1 = r1.getTotalCost();
		double c2 = r2.getTotalCost();

		if (c1 == c2) {

			return r.nextDouble() < probability;
		}
		return c1 < c2;
	}
}
