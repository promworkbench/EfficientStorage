package nl.tue.astar.util;

import gnu.trove.list.TIntList;

public class LinearTrace extends AbstractTrace {

	public LinearTrace(String label, int numEvents) {
		super(label, numEvents);
	}

	public LinearTrace(String label, int[] activitySequence) {
		super(label, activitySequence);
	}

	public LinearTrace(String label, TIntList activitySequence) {
		super(label, activitySequence);
	}

}
