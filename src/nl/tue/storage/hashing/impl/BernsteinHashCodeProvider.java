package nl.tue.storage.hashing.impl;

public class BernsteinHashCodeProvider extends JavaHashCodeProvider {

	public BernsteinHashCodeProvider() {
		super(5381, 33, "Bernstein");
	}

}
