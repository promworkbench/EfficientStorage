package nl.tue.astar;

import gnu.trove.TIntCollection;
import nl.tue.astar.impl.State;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.StorageException;

/**
 * Stores the little bit of information needed per volatile state in the
 * AStarAlgorithm. The memory footprint of this object should be kept to a
 * minimum.
 * 
 * equality should be based purely on the long value of the state and should not
 * include costs. The priorityQueue used in the AStar algorithm will not insert
 * a new Record if it equals an old record and has equal or higher cost
 * (including the estimate)
 * 
 * @author bfvdonge
 * 
 * @param <S>
 */
public interface Record {

	/**
	 * retrieves the state stored at index getState() from the storage.
	 * 
	 * @param storage
	 * @return
	 * @throws StorageException
	 */
	public <H extends Head, T extends Tail> State<H, T> getState(
			CompressedStore<State<H, T>> storage) throws StorageException;

	/**
	 * Returns the index of the state for which this record is kept.
	 * 
	 * @return
	 */
	public long getState();

	/**
	 * returns the cost so far for reaching the corresponding state
	 * 
	 * @return
	 */
	public int getCostSoFar();

	/**
	 * returns an underestimate for the remaining cost
	 * 
	 * @return
	 */
	public double getEstimatedRemainingCost();

	/**
	 * Method should return sum of costSoFar and estimatedCost;
	 */
	public double getTotalCost();

	/**
	 * sets the estimate of the remaining cost. The method should not be called
	 * by classes other than implementations of the AStarThread
	 * 
	 * @return
	 */
	public void setEstimatedRemainingCost(double cost, boolean isExactEstimate);

	/**
	 * true if the last set estimate is exact. If this method returns False, the
	 * Tail implementation of the A Star algorithm has to implement
	 * FastLowerBoundTail
	 * 
	 * @return
	 */
	public boolean isExactEstimate();

	/**
	 * returns the predecessor record.
	 * 
	 * @return
	 */
	public Record getPredecessor();

	/**
	 * puts the index of the state corresponding to this record in the record.
	 * 
	 * @param index
	 */
	public void setState(long index);

	/**
	 * creates a new record, based on the operations m and l applied to the old
	 * head.
	 * 
	 * @param d
	 *            the delegate
	 * @param trace
	 *            TODO
	 * @param trace
	 *            the index of the trace in the log
	 * @param modelMove
	 *            the index of the transition that needs to be fired (or
	 *            Move.BOTTOM if none)
	 * @param activity
	 *            TODO
	 * @param event
	 *            the index of the event in the trace
	 * @return
	 */
	public Record getNextRecord(Delegate<? extends Head, ? extends Tail> d,
			Trace trace, Head newHead, long state, int modelMove,
			int movedEvent, int activity);

	/**
	 * return the id of the modelmove used to reach this record from previous.If
	 * none, then Move.BOTTOM is returned
	 * 
	 * @return
	 */
	public int getModelMove();

	/**
	 * return the index in the trace representing the event that was moved to
	 * get to this step. If none, then Move.BOTTOM is returned
	 * 
	 * @return
	 */
	public int getMovedEvent();

	/**
	 * return the events in the trace that are currently enabled. In case of a
	 * linearly ordered trace, at most 1 event is returned each time.
	 * 
	 * @param delegate
	 * @param trace
	 *            TODO
	 * @return
	 */
	public TIntCollection getNextEvents(
			Delegate<? extends Head, ? extends Tail> delegate, Trace trace);

	/**
	 * returns the length of the path from this record to the root of the search
	 * 
	 * @return
	 */
	public int getBacktraceSize();

}
