package nl.tue.astar;

public interface Delegate<H extends Head, T extends Tail> {

	/**
	 * instantiates a record for the given head. Cost is 0
	 * 
	 * @param head
	 * 
	 * @return
	 */
	public Record createInitialRecord(H head, Trace trace);

	/**
	 * creates the initial tail for the given head.
	 * 
	 * @param head
	 * @return
	 */
	public T createInitialTail(H head);

}