package nl.tue.astar.util;

import gnu.trove.TIntCollection;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

import nl.tue.storage.compressor.BitMask;

public class PartiallyOrderedTrace extends AbstractTrace {

	protected final int[][] predecessors;
	protected int edgeCount = 0;

	public PartiallyOrderedTrace(String label, int numEvents) {
		super(label, numEvents);
		predecessors = new int[numEvents][];
	}

	public PartiallyOrderedTrace(String label, int[] activitySequence) {
		super(label, activitySequence);
		predecessors = new int[activitySequence.length][];
	}

	public PartiallyOrderedTrace(String label, int[] activitySequence, int[][] predecessors) {
		super(label, activitySequence);
		this.predecessors = predecessors;
	}

	/**
	 * sets the predecessors for the event at index event. It is advisable to
	 * provide the predecessors in ascending order.
	 * 
	 * @param event
	 * @param predecessors
	 */
	public void setPredecessors(int event, int... predecessors) {
		this.edgeCount -= (this.predecessors[event] != null ? this.predecessors[event].length : 0);
		this.predecessors[event] = Arrays.copyOf(predecessors, predecessors.length);
		this.edgeCount += predecessors.length;
	}

	/**
	 * returns the predecessors of the event at the given index.
	 * 
	 * @param event
	 * @return
	 */
	public int[] getPredecessors(int index) {
		return this.predecessors[index];
	}

	/**
	 * Returns true if and only if the event at the given index is enabled,
	 * given that those events for which executed is true have been executed.
	 * 
	 * More formally, returns true if and only if for all i in
	 * predecessors[index] holds that executed[i] == true and executed[i] ==
	 * false
	 * 
	 * This method checks the predecessors in descending order, if they are
	 * specified in ascending order.
	 * 
	 * @param index
	 * @param executed
	 * @return
	 */

	public boolean isEnabled(int index, boolean[] executed) {
		assert executed.length == activities.length;
		boolean ok = executed[index] == false;
		if (predecessors[index] == null) {
			return ok;
		}
		for (int i = predecessors[index].length; ok && i-- > 0;) {
			ok &= executed[predecessors[index][i]];
		}
		return ok;
	}

	@Override
	public TIntCollection getNextEvents(boolean[] executed) {
		assert executed.length == activities.length;
		TIntList enabled = new TIntArrayList(activities.length);
		for (int i = 0; i < activities.length; i++) {
			if (isEnabled(i, executed)) {
				enabled.add(i);
			}
		}
		return enabled;
	}

	@Override
	public TIntCollection getNextEvents(BitMask bitMask) {
		return getNextEvents(bitMask.toBooleanArray());
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PartiallyOrderedTrace && Arrays.equals(((PartiallyOrderedTrace) o).activities, activities)
				&& Arrays.deepEquals(((PartiallyOrderedTrace) o).predecessors, predecessors);
	}

	public int hashCode() {
		return Arrays.hashCode(activities) + 37 * Arrays.deepHashCode(predecessors);
	}

	public int getEdgeCount() {
		return edgeCount;
	}
}
