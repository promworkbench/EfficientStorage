package nl.tue.astar.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.tue.astar.Delegate;
import nl.tue.astar.Head;
import nl.tue.astar.Tail;
import nl.tue.astar.impl.memefficient.StorageAwareDelegate;
import nl.tue.astar.impl.memefficient.TailInflater;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.Deflater;

/**
 * Implementation of the tail that implements the Dijkstra estimate for the
 * AStar, i.e. the estimate 0;
 * 
 * @author bfvdonge
 * 
 */
public final class DijkstraTail implements Tail, TailInflater<DijkstraTail>,
		Deflater<DijkstraTail> {

	public static final DijkstraTail EMPTY = new DijkstraTail();

	private DijkstraTail() {

	}

	public Tail getNextTail(Delegate<? extends Head, ? extends Tail> d,
			Head oldHead, int modelMove, int logMove, int activity) {
		return EMPTY;
	}

	public <S> Tail getNextTailFromStorage(
			Delegate<? extends Head, ? extends Tail> d,
			CompressedStore<S> store, long index, int modelMove, int logMove,
			int activity) throws IOException {
		return EMPTY;
	}

	public int getEstimatedCosts(Delegate<? extends Head, ? extends Tail> d,
			Head head) {
		return 0;
	}

	public boolean canComplete() {
		return true;
	}

	public void deflate(DijkstraTail object, OutputStream stream)
			throws IOException {
	}

	public DijkstraTail inflate(InputStream stream) throws IOException {
		return EMPTY;
	}

	public int getMaxByteCount() {
		return -1;
	}

	@Override
	public <H extends Head> int inflateEstimate(
			StorageAwareDelegate<H, DijkstraTail> delegate, H head,
			InputStream stream) throws IOException {
		return 0;
	}

}
