package nl.tue.astar.impl.memefficient;

import nl.tue.astar.Head;
import nl.tue.astar.Tail;
import nl.tue.astar.impl.State;
import nl.tue.astar.impl.StateCompressor;
import nl.tue.storage.CompressedHashSet;
import nl.tue.storage.CompressedStore;
import nl.tue.storage.impl.CompressedStoreHashSetImpl;

public class MemoryEfficientAStarAlgorithm<H extends Head, T extends Tail> {

	private final CompressedHashSet<State<H, T>> statespace;
	private final CompressedStore<State<H, T>> store;
	private final StorageAwareDelegate<H, T> delegate;
	private final StateCompressor<H, T> compressor;
	
	private static int alignment = 0;

	public MemoryEfficientAStarAlgorithm(StorageAwareDelegate<H, T> delegate) {
		this(delegate, 32 * 1024 , 64 * 1024, get_alignment());
	}

	/*
	 * HV: Get the alignment, can be set by the user by providing the ALIGNMENT environment variable.
	 * This alignment can be used to configure the amount of memory the replayer can maximally handle:
	 * ALIGNMENT = 1:  4G
	 *             2:  8G
	 *             3: 12G
	 *             etc.
	 */
	private static int get_alignment() {
		if (alignment == 0) {
			alignment = 8;
			try {
				String alignmentAsString = System.getProperty("nl.tue.astar.impl.memefficient.alignment");
				System.out.println("[MemoryEfficientAStarAlgorithm] nl.tue.astar.impl.memefficient.alignment=" + alignmentAsString);
				if (alignmentAsString == null) {
					alignmentAsString = System.getenv("ALIGNMENT");
					System.out.println("[MemoryEfficientAStarAlgorithm] ALIGNMENT=" + alignmentAsString);
				}
				if (alignmentAsString != null) {
					alignment = Integer.parseInt(alignmentAsString);
					System.out.println("[MemoryEfficientAStarAlgorithm] alignment=" + alignment);
				}
			} catch (Exception e) {
				// Ignore.
			}
			System.out.println("[MemoryEfficientAStarAlgorithm] Aligning on " + alignment + " byte(s).");
		}
		return alignment;
	}
	
	public MemoryEfficientAStarAlgorithm(StorageAwareDelegate<H, T> delegate,
			int blocksize, int initialCapacity, int alignment) {
		this.compressor = new StateCompressor<H, T>(delegate);
		this.delegate = delegate;
		this.statespace = new CompressedStoreHashSetImpl.IntCustomAlignment<State<H, T>>(
				alignment, compressor, compressor, blocksize, compressor,
				compressor, initialCapacity);
		this.store = statespace.getBackingStore();
		delegate.setStateSpace(statespace);
	}

	public CompressedHashSet<State<H, T>> getStatespace() {
		return statespace;
	}

	public CompressedStore<State<H, T>> getStore() {
		return store;
	}

	public StorageAwareDelegate<H, T> getDelegate() {
		return delegate;
	}

}
