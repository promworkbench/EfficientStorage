package nl.tue.astar.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import nl.tue.astar.AStarObserver;
import nl.tue.astar.Head;
import nl.tue.astar.Record;

public class FSMTraceAStarObserver implements AStarObserver {

	protected final String traceFileName;
	protected int path;

	public FSMTraceAStarObserver(String traceFileName) {
		this.traceFileName = traceFileName;
		this.path = 1;
	}

	@Override
	public void nodeVisited(Record node) {
	}

	@Override
	public void edgeTraversed(Record from, Record to) {
	}

	@Override
	public void initialNodeCreated(Record node) {
	}

	@Override
	public void finalNodeFound(Record node) {
		// write the trace file.
		// On each line:
		// a vector of data values
		// a label for an action
		// NIL
		try {
			File f = new File(traceFileName + (path++) + ".trc");
			f.createNewFile();
			OutputStreamWriter parWriter = new OutputStreamWriter(
					new BufferedOutputStream(new FileOutputStream(f)));
			List<Record> path = new ArrayList<Record>();
			path.add(node);
			while (node.getPredecessor() != null) {
				path.add(node.getPredecessor());
				node = node.getPredecessor();
			}

			for (int i = path.size() - 1; i-- > 0;) {
				parWriter.append(FSMGraphAStarObserver.toString(path.get(i)
						.getPredecessor(), path.get(i)));
				if (i != 0) {
					parWriter.append("\n");
				}
			}
			parWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void stoppedUnreliablyAt(Record rec) {
	}

	@Override
	public void close() {
	}

	@Override
	public void estimateComputed(Head head) {
		// skip
	}

}
