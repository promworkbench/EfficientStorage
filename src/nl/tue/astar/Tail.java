package nl.tue.astar;

import java.io.IOException;

import nl.tue.storage.CompressedStore;

/**
 * The tail provides the actual estimate for a head. Implementations can choose
 * to compute the estimate each time, but a tail can also be statefull to speed
 * up the estimation process.
 * 
 * A default implementation of a Tail is available in the form of
 * nl.tue.astar.impl.DijkstraTail, which always returns 0 as an estimate.
 * 
 * @author bfvdonge
 * 
 */
public interface Tail {

	/**
	 * constructs the new tail based on the two operations from the old head.
	 * Preferably, the code to compute the new tail is kept as lightweight as
	 * possible.
	 * 
	 * @param oldHead
	 * @param m
	 * @param l
	 * @return
	 */
	public Tail getNextTail(Delegate<? extends Head, ? extends Tail> d,
			Head newHead, int modelMove, int logMove, int activity);

	/**
	 * constructs the new tail based on the two operations from the old state,
	 * which is stored in the given store at the given index. Preferably, the
	 * code to compute the new tail is kept as lightweight as possible.
	 * 
	 * @param <S>
	 * @param store
	 * @param index
	 * @param m
	 * @param l
	 * @return
	 */
	public <S> Tail getNextTailFromStorage(
			Delegate<? extends Head, ? extends Tail> d,
			CompressedStore<S> store, long index, int modelMove, int logMove,
			int activity) throws IOException;

	/**
	 * get an estimate of the remaining distance to the target state in the
	 * search.
	 * 
	 * @return
	 */
	public int getEstimatedCosts(Delegate<? extends Head, ? extends Tail> d,
			Head head);

	/**
	 * returns true if and only if the replay can finish according to this tail,
	 * i.e. for the case
	 * 
	 * @return
	 */
	public boolean canComplete();

}
