package nl.tue.astar.impl;

import java.io.File;
import java.io.IOException;

import nl.tue.astar.AStarThread;
import nl.tue.astar.Record;

public class DotSpanningTreeObserver extends DotGraphAStarObserver {

	public DotSpanningTreeObserver(File outputFile) {
		super(outputFile);
	}

	@Override
	public void nodeVisited(Record node) {
		try {
			writer.write("n"
					+ node.getState()
					+ " [label=<n"
					+ node.getState()
					+ (node.getPredecessor() != null ? "<BR/>h:"
							+ node.getEstimatedRemainingCost() : "") + "<BR/>("
					+ (++visitCounter) + ")>,style=filled,color=lightgray];");
			if (node.getPredecessor() != null) {
				String parameters;
				if (node.getModelMove() == AStarThread.NOMOVE) {
					parameters = "color=gold2";
				} else if (node.getMovedEvent() == AStarThread.NOMOVE) {
					parameters = "color=magenta2";
				} else {
					parameters = "color=limegreen";
				}

				writer.write("n"
						+ node.getPredecessor().getState()
						+ " -> n"
						+ node.getState()
						+ " [penwidth=2.0,label=\""
						+ (node.getCostSoFar() - node.getPredecessor()
								.getCostSoFar()) + "\"," + parameters + "];");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void edgeTraversed(Record from, Record to) {
		// ignore
	}

	@Override
	protected void addPathToRoot(Record node) throws IOException {
		// ignore
	}

}
