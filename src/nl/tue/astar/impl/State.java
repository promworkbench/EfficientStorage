package nl.tue.astar.impl;

import nl.tue.astar.Head;
import nl.tue.astar.Tail;

/**
 * A State can be constructed with or without a tail
 * 
 * Both equals and hashcode are defined on the head only
 * 
 * The tail is not guaranteed to be non-null
 * 
 * @author bfvdonge
 * 
 */
public final class State<H extends Head, T extends Tail> {

	private final H head;
	private final T tail;

	public State(H head, T tail) {
		assert head != null;
		this.head = head;
		this.tail = tail;
	}

	/**
	 * returns the head
	 * 
	 * @return
	 */
	public H getHead() {
		return head;
	}

	/**
	 * returns the tail, might return null
	 * 
	 * @return
	 */
	public T getTail() {
		return tail;
	}

	public boolean equals(Object o) {
		return (o != null) && (o instanceof State)
				&& ((State<?, ?>) o).getHead().equals(head);
	}

	public int hashCode() {
		return head.hashCode();
	}

	public String toString() {
		return head.toString() + "," + tail.toString();
	}
}
