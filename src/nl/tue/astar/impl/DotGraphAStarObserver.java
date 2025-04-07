package nl.tue.astar.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import nl.tue.astar.AStarObserver;
import nl.tue.astar.AStarThread;
import nl.tue.astar.FastLowerBoundTail;
import nl.tue.astar.Head;
import nl.tue.astar.ObservableAStarThread;
import nl.tue.astar.Record;
import nl.tue.storage.CompressedStore;

public class DotGraphAStarObserver implements AStarObserver {

	protected Writer writer;
	protected int visitCounter;
	protected int finalNodeCounter;
	protected final File outputFile;
	private CompressedStore<? extends State<?, ?>> store;

	public DotGraphAStarObserver(File outputFile,
			CompressedStore<? extends State<?, ?>> store) {
		this.outputFile = outputFile;
		this.store = store;
	}

	public DotGraphAStarObserver(File outputFile) {
		this(outputFile, null);
	}

	@Override
	public void nodeVisited(Record node) {
		try {
			String b = "";
			State<?, ?> state = null;
			if (store != null) {
				state = store.getObject(node.getState());
				if (state.getTail() instanceof FastLowerBoundTail) {
					if (!((FastLowerBoundTail) state.getTail())
							.isExactEstimateKnown()) {
						b = "|";
					}
				}
			}
			writer.write("n"
					+ node.getState()
					+ " [label=<n"
					+ node.getState()
					+ "<BR/>"
					+ "<BR/>"
					+ (state != null ? state.getHead().toString() : "")
					+ (node.getPredecessor() != null ? "<BR/>" + b + "h:"
							+ node.getEstimatedRemainingCost() + b : "")
					+ "<BR/>(" + (++visitCounter)
					+ ")>,style=filled,color=lightgray];");
		} catch (Exception e) {

		}
	}

	@Override
	public void edgeTraversed(Record from, Record to) {
		try {
			String parameters;
			int w = 100;
			if (to.getModelMove() == AStarThread.NOMOVE) {
				if (to != from) {
					writer.write("{ rank = same; n" + from.getState() + "; n"
							+ to.getState() + "; }\n");
				}
				parameters = "color=gold2";// ,constraint=false";
			} else if (to.getMovedEvent() == AStarThread.NOMOVE) {
				parameters = "color=magenta2";
			} else {
				parameters = "color=limegreen";
				w = 50;
			}

			writer.write("n" + from.getState() + " -> n" + to.getState());

			if (from != to) {
				writer.write(" [penwidth=2.0,label=\""
						+ (to.getCostSoFar() - from.getCostSoFar())
						+ to.toString()
						+ "\","
						+ (to.getEstimatedRemainingCost() == ObservableAStarThread.ESTIMATEIRRELEVANT ? "arrowhead=teenormal,weight=1,penwidth=0.5,"
								: "weight=" + w + ",") + parameters + "]\n");
			} else {
				writer.write(" [label=\"" + to.getEstimatedRemainingCost()
						+ "\"]\n");
			}
			writer.write(";\n");
			if (to.getEstimatedRemainingCost() != ObservableAStarThread.ESTIMATEIRRELEVANT) {
				String b = to.isExactEstimate() ? "" : "|";
				writer.write("n" + to.getState() + " [label=<n" + to.getState()
						+ "<BR/>h:" + b + to.getEstimatedRemainingCost() + b
						+ ">];\n");
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void initialNodeCreated(Record node) {
		try {

			visitCounter = 0;
			finalNodeCounter = 0;
			outputFile.createNewFile();
			OutputStream stream = new BufferedOutputStream(
					new FileOutputStream(outputFile));
			this.writer = new OutputStreamWriter(stream);
			writer.append("digraph G { ");
			writer.write("n" + node.getState() + " [peripheries=2];\n");
			writer.write("initial [shape=box,label=\"estimate: "
					+ node.getEstimatedRemainingCost() + "\"];\n");
		} catch (IOException e) {
			this.writer = null;
		}
	}

	@Override
	public void finalNodeFound(Record node) {
		finalNodeCounter++;
		try {
			writer.write("n" + node.getState() + " [peripheries=2];");
			addPathToRoot(node);
		} catch (Exception e) {
		}
	}

	public void close() {
		try {
			if (writer != null) {
				writer.write("}");
				writer.close();
			}
		} catch (Exception e) {
		}
	}

	protected void addPathToRoot(Record node) throws IOException {
		while (node.getPredecessor() != null) {
			writer.write("n"
					+ node.getPredecessor().getState()
					+ " -> n"
					+ node.getState()
					+ " [weight=20,penwidth=5.0,style=dashed,color=red,arrowhead=none,arrowtail=none];");
			node = node.getPredecessor();
		}
	}

	@Override
	public void stoppedUnreliablyAt(Record node) {
		if (finalNodeCounter > 0) {
			return;
		}
		finalNodeCounter++;
		try {
			if (node != null) {
				writer.write("n" + node.getState()
						+ " [peripheries=2,shape=diamond];");
				addPathToRoot(node);
			}
			writer.write("}");
			writer.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void estimateComputed(Head head) {
		// skip
	}

}
