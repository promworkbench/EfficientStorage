package nl.tue.storage;

import java.io.IOException;

public class StorageException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8047024179220310696L;

	public StorageException(String message) {
		super(message);
	}

	public StorageException(IOException e) {
		super("IO Exception from storage", e);
	}
}
