package nl.tue.astar.impl.memefficient;

import java.io.IOException;
import java.io.InputStream;

import nl.tue.astar.Head;
import nl.tue.astar.Tail;
import nl.tue.storage.Inflater;

public interface TailInflater<T extends Tail> extends Inflater<T> {

	public <H extends Head> int inflateEstimate(
			StorageAwareDelegate<H, T> delegate, H head, InputStream stream)
			throws IOException;

}
