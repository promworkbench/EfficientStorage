package nl.tue.astar;

import nl.tue.storage.compressor.BitMask;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;

/**
 * interface to represent a trace.
 * 
 * @author bfvdonge
 * 
 */
public interface Trace {

	/**
	 * returns the activity at the given index
	 * 
	 * @param index
	 * @return
	 */
	public int get(int index);

	/**
	 * returns the number of events in the trace
	 * 
	 * @return
	 */
	public int getSize();

	/**
	 * Returns a TIntCollection containing all events that are currently
	 * enabled, given a boolean array indicating which events have been executed
	 * so far.
	 * 
	 * @param executed
	 * @return
	 */
	public TIntCollection getNextEvents(boolean[] executed);

	/**
	 * Returns a TIntCollection containing all events that are currently
	 * enabled, given a bitmask for a boolean
	 * 
	 * @param executed
	 * @return
	 */
	public TIntCollection getNextEvents(BitMask bitMask);

	/**
	 * Iterator to iterate over the list of activities, assuming a total order.
	 * The iterator should throw an exception on remove().
	 * 
	 * @return
	 */
	public TIntIterator iterator();

	/**
	 * returns the label of the trace
	 * @return
	 */
	public String getLabel();
}