package org.dotspace.oofp.util.functional.impl;

import java.util.Map;
import java.util.function.BiConsumer;

public class HashMapPutter<K, V> implements BiConsumer<Map<K, V>, V> {

	private K key;

	public HashMapPutter(K key) {
		super();
		this.key = key;
	}

	@Override
	public void accept(Map<K, V> m, V v) {
		m.put(key, v);
	}

}
