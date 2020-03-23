package org.dotspace.creation;

public interface AccessingPolicy<T, V> {

	public void access(T instance);
	
}
