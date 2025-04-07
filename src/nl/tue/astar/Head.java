package nl.tue.astar;

import gnu.trove.list.TIntList;

/**
 * The head determines the state in the search space. It should contain all
 * information to base an estimate on, but providing the actual estimate is not
 * part of the head but of the tail. This allows to separate these
 * implementations to have multiple estimation strategies for a given head.
 * 
 * @author bfvdonge
 * 
 */
public interface Head {

	/**
	 * Produce the next head, given the moves.
	 * 
	 * If logMove == AStarThread.NOMOVE, then modelMove is an element of the
	 * TIntList returned by the last call to getModelMoves() and activity ==
	 * AStarThread.NOMOVE.
	 * 
	 * If modelMove == AStarThread.NOMOVE, then logMove is an index into the
	 * trace on which the AStarThread is working and activity is the
	 * corresponding activity.
	 * 
	 * If logMove!=AStarThread.NOMOVE and modelMove != AStarThread.NOMOVE, then
	 * modelMove is an element of the TIntlist returned by the last call to
	 * getSynchronousMove, logMove is an index into the trace on which the
	 * AStarThread is working and activity is the corresponding activity.
	 * 
	 * @param rec
	 * @param d
	 * @param modelMove
	 * @param logMove
	 * @param activity
	 * @return
	 */
	public Head getNextHead(Record rec,
			Delegate<? extends Head, ? extends Tail> d, int modelMove,
			int logMove, int activity);

	/**
	 * get the synchronous moves that are possible on activity, given the
	 * current list of enabled modelMoves. The list enabled is obtained through
	 * a previous call to getModelMoves and implementations may only remove
	 * elements, i.e. the implementation may decide to remove modelMoves based
	 * on the available synchronous moves.
	 * 
	 * @param delegate
	 * @param enabled
	 *            TODO
	 * @param activity
	 * @return
	 */
	public TIntList getSynchronousMoves(Record rec,
			Delegate<? extends Head, ? extends Tail> delegate,
			TIntList enabled, int activity);

	/**
	 * get the model moves that are possible.
	 * 
	 * @param delegate
	 * @return
	 */
	public TIntList getModelMoves(Record rec,
			Delegate<? extends Head, ? extends Tail> delegate);

	/**
	 * checks if this head belongs to a final state;
	 * 
	 * @return
	 */
	public boolean isFinal(Delegate<? extends Head, ? extends Tail> delegate);

}
