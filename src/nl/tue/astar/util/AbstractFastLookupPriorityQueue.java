package nl.tue.astar.util;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;

import nl.tue.astar.Record;

public abstract class AbstractFastLookupPriorityQueue implements FastLookupPriorityQueue {

	protected final TLongIntMap locationMap;
	protected static final int NEV = -1;
	/**
	 * Priority queue represented as a balanced binary heap: the two children of
	 * queue[n] are queue[2*n+1] and queue[2*(n+1)]. The priority queue is
	 * ordered by the record's natural ordering: For each node n in the heap and
	 * each descendant d of n, n <= d. The element with the best value is in
	 * queue[0], assuming the queue is nonempty.
	 */
	protected Record[] queue;
	/**
	 * The number of elements in the priority queue.
	 */
	protected int size = 0;

	/**
	 * The maximum total cost for any record in this queue. If the cost of a
	 * record which is added is higher that this value, it is not added
	 */
	protected int maxCost;

	public AbstractFastLookupPriorityQueue(int initialCapacity) {
		this(initialCapacity, Integer.MAX_VALUE);
	}

	public AbstractFastLookupPriorityQueue(int initialCapacity, int maxCost) {
		this.maxCost = maxCost;
		locationMap = new TLongIntHashMap(initialCapacity, 0.5f, -1, NEV);
		this.queue = new Record[initialCapacity];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.tue.astar.util.FastLookupPriorityQueue#isEmpty()
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	public int hashCode() {
		return locationMap.hashCode();
	}

	public void setMaxCost(int maxCost) {
		this.maxCost = maxCost;
	}

	public int getMaxCost() {
		return this.maxCost;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.tue.astar.util.FastLookupPriorityQueue#contains(nl.tue.astar.Record)
	 */
	public Record contains(Record newRec) {
		int location = locationMap.get(newRec.getState());
		if (location != NEV) {
			return peek(location);
		} else {
			return null;
		}
	}

	public boolean checkInv() {
		return checkInv(0);
	}

	/**
	 * Increases the capacity of the array.
	 * 
	 * @param minCapacity
	 *            the desired minimum capacity
	 */
	protected void grow(int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError();
		int oldCapacity = queue.length;
		// Double size if small; else grow by 50%
		int newCapacity = ((oldCapacity < 64) ? ((oldCapacity + 1) * 2) : ((oldCapacity / 2) * 3));
		if (newCapacity < 0) // overflow
			newCapacity = Integer.MAX_VALUE;
		if (newCapacity < minCapacity)
			newCapacity = minCapacity;
		queue = Arrays.copyOf(queue, newCapacity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.tue.astar.util.FastLookupPriorityQueue#peek()
	 */
	public Record peek() {
		if (size == 0)
			return null;
		return queue[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.tue.astar.util.FastLookupPriorityQueue#size()
	 */
	public int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.tue.astar.util.FastLookupPriorityQueue#poll()
	 */
	public Record poll() {
		if (size == 0)
			return null;
		int s = --size;
		Record result = queue[0];
		Record x = queue[s];
		queue[s] = null;
		locationMap.remove(result.getState());

		if (s != 0)
			siftDown(0, x);
		return result;
	}

	protected Record peek(int location) {
		return queue[location];
	}

	public String toString() {
		return Arrays.toString(queue);
	}

	protected void updateToBetter(int index, Record newRecord) {
		assert index >= 0 && index < size;
		locationMap.put(newRecord.getState(), index);
		siftUp(index, newRecord);
	}

	/**
	 * Inserts the specified element into this priority queue.
	 * 
	 * @return {@code true} (as specified by {@link Collection#add})
	 * @throws ClassCastException
	 *             if the specified element cannot be compared with elements
	 *             currently in this priority queue according to the priority
	 *             queue's ordering
	 * @throws NullPointerException
	 *             if the specified element is null
	 */
	public boolean add(Record newE) {
		if (newE.getTotalCost() > maxCost) {
			return false;
		}
		// check if overwrite is necessary, i.e. only add if the object does not
		// exist yet,
		// or exists, but with higher costs.
		int location = locationMap.get(newE.getState());
		if (location == NEV) { // XXX: For limited size queues: || location >= size) {
			// new element, add to queue and return
			offer(newE);
			// assert checkInv();
			return true;
		}

		if (isBetter(newE, peek(location))) {
			// update to better, if newE better then peek(location)
			updateToBetter(location, newE);
			// assert checkInv();
			return true;
		}
		return false;
	}

	protected abstract boolean isBetter(Record r1, Record r2);

	/**
	 * Inserts the specified element into this priority queue.
	 * 
	 * @return {@code true} (as specified by {@link Queue#offer})
	 * @throws ClassCastException
	 *             if the specified element cannot be compared with elements
	 *             currently in this priority queue according to the priority
	 *             queue's ordering
	 * @throws NullPointerException
	 *             if the specified element is null
	 */
	protected void offer(Record e) {
		if (e == null)
			throw new NullPointerException();
		int i = size;
		if (i >= queue.length)
			grow(i + 1);
		size = i + 1;
		if (i == 0) {
			queue[0] = e;
			locationMap.put(e.getState(), 0);
		} else
			siftUp(i, e);

		//		// Added to limit the size of the priority queue
		//		if (size > 3) {
		//		for (int s=3; s<size; s++) {
		//			locationMap.remove(queue[s].getState());
		//		}
		//			size = 3;
		//			
		//		}
	}

	/**
	 * Inserts item x at position k, maintaining heap invariant by promoting x
	 * up the tree until it is greater than or equal to its parent, or is the
	 * root.
	 * 
	 * @param k
	 * @param x
	 *            the item to insert
	 */
	protected void siftUp(int k, Record x) {
		while (k > 0) {
			int parent = (k - 1) >>> 1;
			Record e = queue[parent];
			if (!isBetter(x, e)) {
				break;
			}
			queue[k] = e;
			locationMap.put(e.getState(), k);
			k = parent;
		}
		queue[k] = x;
		locationMap.put(x.getState(), k);
	}

	/**
	 * Inserts item x at position k, maintaining heap invariant by demoting x
	 * down the tree repeatedly until it is less than or equal to its children
	 * or is a leaf.
	 * 
	 * @param k
	 *            the position to fill
	 * @param x
	 *            the item to insert
	 */
	protected void siftDown(int k, Record x) {
		int half = size >>> 1;
		while (k < half) {
			int child = (k << 1) + 1;
			Record c = queue[child];
			int right = child + 1;
			if (right < size && isBetter(queue[right], c))
				c = queue[child = right];

			if (!isBetter(c, x))
				break;

			queue[k] = c;
			// assert locationMap.get(c.getState()) == child;
			// i.e. child + k -child == k,
			// hence we use adjustValue instead of put here.
			locationMap.adjustValue(c.getState(), k - child);
			k = child;
		}
		queue[k] = x;
		locationMap.put(x.getState(), k);
	}

	protected boolean checkInv(int loc) {
		Record n = queue[loc];
		Record c1 = null;
		Record c2 = null;
		if (2 * loc + 1 < queue.length)
			c1 = queue[2 * loc + 1];

		if (2 * (loc + 1) < queue.length)
			c2 = queue[2 * (loc + 1)];

		return (c1 == null ? true : !isBetter(c1, n) && checkInv(2 * loc + 1))
				&& (c2 == null ? true : !isBetter(c2, n) && checkInv(2 * (loc + 1)));

	}
}