package nl.tue.astar.util;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;

import nl.tue.astar.Trace;
import nl.tue.storage.compressor.BitMask;
import nl.tue.storage.compressor.TBooleanIterator;

public abstract class AbstractTrace implements Trace {

	protected final String label;

	protected final int[] activities;

	public AbstractTrace(String label, int numEvents) {
		this.label = label;
		activities = new int[numEvents];
	}

	public AbstractTrace(String label, int[] activitySequence) {
		this.label = label;
		this.activities = Arrays.copyOf(activitySequence, activitySequence.length);
	}

	public AbstractTrace(String label, TIntList activitySequence) {
		this.label = label;
		this.activities = activitySequence.toArray();
	}

	public int get(int index) {
		return activities[index];
	}

	public TIntCollection getNextEvents(boolean[] executed) {
		assert executed.length == activities.length;
		TIntList enabled = new TIntArrayList(1);
		int i = 0;
		while (i < executed.length && executed[i]) {
			i++;
		}
		if (i < executed.length) {
			enabled.add(i);
		}
		return enabled;
	}

	@Override
	public TIntCollection getNextEvents(BitMask bitMask) {
		TIntList enabled = new TIntArrayList(1);
		int i = 0;
		TBooleanIterator it = bitMask.iterator();
		while (it.hasNext()) {
			if (!it.next()) {
				enabled.add(i);
				break;
			}
			i++;
		}
		return enabled;
	}

	@Override
	public int getSize() {
		return activities.length;
	}

	/**
	 * sets the activity at index index
	 * 
	 * @param index
	 * @param act
	 */
	public void set(int index, int act) {
		activities[index] = act;
	}

	@Override
	public TIntIterator iterator() {
		return new TIntIterator() {
			private int next = 0;

			@Override
			public void remove() {
				throw new UnsupportedOperationException("cannot remove elements from a trace.");
			}

			@Override
			public boolean hasNext() {
				return next < activities.length;
			}

			@Override
			public int next() {
				return activities[next++];
			}
		};

	}

	public boolean equals(Object o) {
		return o instanceof LinearTrace && Arrays.equals(((LinearTrace) o).activities, activities);
	}

	public String toString() {
		return Arrays.toString(activities);
	}

	public int hashCode() {
		return Arrays.hashCode(activities);
	}

	public String getLabel() {
		return label;
	}

}
