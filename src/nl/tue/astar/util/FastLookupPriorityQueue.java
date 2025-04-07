package nl.tue.astar.util;

import nl.tue.astar.Record;

public interface FastLookupPriorityQueue {

	public boolean isEmpty();

	/**
	 * Checks if the queue contains a record pointing to the same state as the
	 * given record. If so, it returns that record, if not, it returns null;
	 * 
	 * @param newRec
	 * @return
	 */
	public Record contains(Record newRec);

	public Record peek();

	public int size();

	public Record poll();

	public boolean add(Record newE);

	public void setMaxCost(int maxCost);

	public int getMaxCost();

}