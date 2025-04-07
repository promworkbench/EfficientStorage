package nl.tue.astar;

public interface AStarObserver {

	public void nodeVisited(Record node);

	public void edgeTraversed(Record from, Record to);

	public void estimateComputed(Head head);

	public void initialNodeCreated(Record node);

	public void finalNodeFound(Record node);

	public void stoppedUnreliablyAt(Record rec);

	/**
	 * Needs to be called after the observer is no longer needed. This is
	 * independent of the use of the observer and this method is never called
	 * from within an ObservableAStarThread
	 */
	public void close();

}
