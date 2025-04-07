package nl.tue.astar.impl.memefficient;

import nl.tue.astar.FastLowerBoundTail;
import nl.tue.astar.Head;

public interface CachedStorageAwareDelegate<H extends Head, T extends FastLowerBoundTail>
		extends StorageAwareDelegate<H, T> {

	/**
	 * additional requirement on the defater to allow for overwriting previously
	 * written objects.
	 */
	public HeadDeflater<H> getHeadDeflater();

}
