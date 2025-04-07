package nl.tue.storage.hashing;

import nl.tue.astar.util.ShortShortMultiset;

public interface HashCodeProvider {

	public int hash(ShortShortMultiset... sets);

	public int hash(short[]... sets);

	public int hash(int[]... sets);

}