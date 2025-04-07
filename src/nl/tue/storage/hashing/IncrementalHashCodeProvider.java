package nl.tue.storage.hashing;

public interface IncrementalHashCodeProvider extends HashCodeProvider {

	/**
	 * Updates the old hash value and returns the new one, assuming that the
	 * value at index idx was changed from oldVal to newVal. Note that idx
	 * should be an index into the concatenation of the arrays previously
	 * provided in the call to hash().
	 * 
	 * @param oldHash
	 * @param idx
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public int updateHash(int oldHash, int idx, short oldVal, short newVal);

	/**
	 * Updates the old hash value and returns the new one, assuming that the
	 * value at index idx was changed from oldVal to newVal. Note that idx
	 * should be an index into the concatenation of the arrays previously
	 * provided in the call to hash().
	 * 
	 * @param oldHash
	 * @param idx
	 * @param oldVal
	 * @param newVal
	 * @return
	 */
	public int updateHash(int oldHash, int idx, int oldVal, int newVal);

}
