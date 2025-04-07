package nl.tue.astar;


public interface FastLowerBoundTail extends Tail {

	/**
	 * Starts the computation of the exact estimate on the first call. The
	 * method should not return before the exact estimate is known. The exact
	 * estimate should never become lower than the previously computed
	 * lowerbound. Every additional call to computeEstimate should be ignored.
	 * 
	 * @param d
	 * @param head
	 */
	public void computeEstimate(
			Delegate<? extends Head, ? extends Tail> delegate, Head head,
			int lastEstimate);

	/**
	 * get an estimate of the remaining distance to the target state in the
	 * search. This method should return a fast lower bound, until the
	 * computeEstimate() method has been called. After that, the exact estimate
	 * should be returned
	 * 
	 * @return
	 */
	public int getEstimatedCosts(
			Delegate<? extends Head, ? extends Tail> delegate, Head head);

	/**
	 * This method should return true if the exact estimate is known. It should
	 * return true after the first call to ComputeEstimate, but may return true
	 * ealier.
	 * 
	 * @return
	 */
	public boolean isExactEstimateKnown();
}
