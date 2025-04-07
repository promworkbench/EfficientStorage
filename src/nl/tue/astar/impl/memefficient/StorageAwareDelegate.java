package nl.tue.astar.impl.memefficient;

import nl.tue.astar.Delegate;
import nl.tue.astar.Head;
import nl.tue.astar.Tail;
import nl.tue.astar.impl.State;
import nl.tue.storage.CompressedHashSet;
import nl.tue.storage.Deflater;
import nl.tue.storage.EqualOperation;
import nl.tue.storage.HashOperation;
import nl.tue.storage.Inflater;

public interface StorageAwareDelegate<H extends Head, T extends Tail> extends Delegate<H, T> {

	public Inflater<H> getHeadInflater();

	public Deflater<H> getHeadDeflater();

	public TailInflater<T> getTailInflater();

	public Deflater<T> getTailDeflater();

	public HashOperation<State<H, T>> getHeadBasedHashOperation();

	public EqualOperation<State<H, T>> getHeadBasedEqualOperation();

	public void setStateSpace(CompressedHashSet<State<H, T>> statespace);

}
