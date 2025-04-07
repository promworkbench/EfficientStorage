package nl.tue.astar.impl;

import gnu.trove.map.TDoubleIntMap;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TDoubleIntHashMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TLongIntHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import nl.tue.astar.AStarObserver;
import nl.tue.astar.Head;
import nl.tue.astar.ObservableAStarThread;
import nl.tue.astar.Record;

public class FSMGraphAStarObserver implements AStarObserver {

	protected final File outputFile;
	protected final File transitionFile;
	protected final File stateFile;
	protected final TLongIntMap state2index;
	protected final TDoubleIntMap est2index;
	protected final TIntLongMap index2state;
	protected final TIntDoubleMap index2est;

	protected int curState;
	protected int curEst;

	protected Writer tranWriter;
	protected Writer stateWriter;

	public FSMGraphAStarObserver(File outputFile) throws IOException {
		this.outputFile = outputFile;
		this.transitionFile = File.createTempFile("fmsPar", ".fsm");
		this.stateFile = File.createTempFile("fmsPar", ".fsm");

		this.state2index = new TLongIntHashMap();
		this.est2index = new TDoubleIntHashMap();
		this.index2state = new TIntLongHashMap();
		this.index2est = new TIntDoubleHashMap();
	}

	@Override
	public void nodeVisited(Record node) {
		// ignore
	}

	@Override
	public void edgeTraversed(Record from, Record to) {
		// write the state

		try {
			int fromState = state2index.get(from.getState());
			int toState;
			double toEst;
			if (state2index.containsKey(to.getState())) {
				toState = state2index.get(to.getState());
				toEst = est2index.get(to.getEstimatedRemainingCost());
			} else {
				toState = curState;
				state2index.put(to.getState(), curState);
				index2state.put(curState, to.getState());
				curState++;

				assert (to.getEstimatedRemainingCost() != ObservableAStarThread.ESTIMATEIRRELEVANT);

				if (!est2index.containsKey(to.getEstimatedRemainingCost())) {
					toEst = curEst;
					est2index.put(to.getEstimatedRemainingCost(), curEst);
					index2est.put(curEst, to.getEstimatedRemainingCost());
					curEst++;
				} else {
					toEst = est2index.get(to.getEstimatedRemainingCost());
				}

				// write the state.
				stateWriter.append((toEst) + "\n");

			}

			// write the edge
			tranWriter.append(fromState + " ");
			tranWriter.append(toState + " ");
			tranWriter.append(toString(from, to));
			tranWriter.append("\"\n");
		} catch (Exception e) {
		}

	}

	public static String toString(Record from, Record to) {
		return "\"d: "
				+ (to.getCostSoFar() - from.getCostSoFar())
				+ " ("
				+ to.getMovedEvent()
				+ ","
				+ to.getModelMove()
				+ (to.getEstimatedRemainingCost() == ObservableAStarThread.ESTIMATEIRRELEVANT ? ")|"
						: ")");
	}

	@Override
	public void initialNodeCreated(Record node) {
		try {
			curEst = 1;
			curState = 1;
			tranWriter = new OutputStreamWriter(new BufferedOutputStream(
					new FileOutputStream(transitionFile)));
			stateWriter = new OutputStreamWriter(new BufferedOutputStream(
					new FileOutputStream(stateFile)));

			stateWriter.append(curState + "\n");

			state2index.put(node.getState(), curState);
			index2state.put(curState, node.getState());
			curState++;

			est2index.put(node.getEstimatedRemainingCost(), curEst);
			index2est.put(curEst, node.getEstimatedRemainingCost());
			curEst++;

		} catch (IOException e) {
			tranWriter = null;
			stateWriter = null;
		}
	}

	@Override
	public void finalNodeFound(Record node) {

	}

	@Override
	public void stoppedUnreliablyAt(Record rec) {
		// ignore

	}

	@Override
	public void close() {
		try {
			tranWriter.close();
			stateWriter.close();

			outputFile.createNewFile();
			OutputStream out = new BufferedOutputStream(new FileOutputStream(
					outputFile));

			// write parameters
			OutputStreamWriter parWriter = new OutputStreamWriter(out);
			// parWriter.append("s(" + (index2state.keys().length) +
			// ") State ");
			// for (int i = 1; i <= index2state.keys().length; i++) {
			// parWriter.append("\"" + index2state.get(i) + "\" ");
			// }
			// parWriter.append("\n");

			parWriter.append("h(" + (index2est.keys().length + 1)
					+ ") Est \"?\" ");
			for (int i = 1; i <= index2est.keys().length; i++) {
				parWriter.append("\"" + index2est.get(i) + "\" ");
			}
			parWriter.append("\n");
			parWriter.append("---\n");
			parWriter.flush();
			// copy states
			InputStream in = new BufferedInputStream(new FileInputStream(
					stateFile));
			byte[] buf = new byte[4 * 1024];
			int len = 0;
			while ((len = in.read(buf)) >= 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.flush();
			parWriter.append("---\n");
			parWriter.flush();
			// copy edges
			in = new BufferedInputStream(new FileInputStream(transitionFile));
			len = 0;
			while ((len = in.read(buf)) >= 0) {
				out.write(buf, 0, len);
			}
			in.close();

			parWriter.close();

		} catch (IOException e) {

		}

	}

	@Override
	public void estimateComputed(Head head) {
		// skip
	}

}
