package nl.tue.astar.util;

import nl.tue.astar.Record;

public class DepthFirstFastLookupPriorityQueue extends AbstractFastLookupPriorityQueue {

	protected final boolean useExactEstimatePriority;

	/**
	 * Creates a {@code PriorityQueue} with the specified initial capacity that
	 * orders its elements according to the specified comparator.
	 * 
	 * @param initialCapacity
	 *            the initial capacity for this priority queue
	 * @throws IllegalArgumentException
	 *             if {@code initialCapacity} is less than 1
	 */

	public DepthFirstFastLookupPriorityQueue(int initialCapacity) {
		this(initialCapacity, false);
	}

	public DepthFirstFastLookupPriorityQueue(int initialCapacity, int maxCost) {
		this(initialCapacity, maxCost, false);
	}

	public DepthFirstFastLookupPriorityQueue(int initialCapacity, boolean useExactEstimatePriority) {
		super(initialCapacity);
		this.useExactEstimatePriority = useExactEstimatePriority;
	}

	public DepthFirstFastLookupPriorityQueue(int initialCapacity, int maxCost, boolean useExactEstimatePriority) {
		super(initialCapacity, maxCost);
		this.useExactEstimatePriority = useExactEstimatePriority;
	}

	@Override
	public boolean isBetter(Record r1, Record r2) {
		// retrieve stored cost
		double c1 = r1.getTotalCost();
		double c2 = r2.getTotalCost();

		if (c1 < c2) {
			// First order sorting: On total costs
			return true; //
		} else if (c1 == c2) {

			// Second order sorting
			if (useExactEstimatePriority && r1.isExactEstimate() != r2.isExactEstimate()) {
				// Same total cost, but only one of the two is certain.
				// If that is r1, then t1 is better. If that is r2, then r1 is NOT better.
				return r1.isExactEstimate();
			}
			// Both are certain or uncertain in their estimate.

			// Third order sorting:
			if (r1.getCostSoFar() > r2.getCostSoFar()) {
				// if cost so far
				return true;
			}
		}
		return false;
	}

	public boolean equals(Object o) {
		return (o instanceof DepthFirstFastLookupPriorityQueue)
				&& ((DepthFirstFastLookupPriorityQueue) o).locationMap.equals(locationMap)
				&& ((DepthFirstFastLookupPriorityQueue) o).useExactEstimatePriority == useExactEstimatePriority;
	}

}
