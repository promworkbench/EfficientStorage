package nl.tue.astar.impl.memefficient;

import java.io.IOException;

import nl.tue.astar.Head;
import nl.tue.storage.Deflater;
import nl.tue.storage.impl.SkippableOutputStream;

public interface HeadDeflater<H extends Head> extends Deflater<H> {

	/**
	 * Skips as many bytes in the stream as are required to store the given head
	 * 
	 * @param head
	 * @param out
	 */
	public void skip(H head, SkippableOutputStream out) throws IOException;
}
