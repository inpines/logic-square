package org.dotspace.oofp.support.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AdtManipulations {

	public static <K, V> Supplier<Map<K, V>> newMap() {
		return () -> new HashMap<K, V>();
	}
	
	public static <T> Supplier<List<T>> newList() {
		return () -> new ArrayList<T>();
	}
	
	public static <K, V> BiConsumer<Map<K, V>, V> putValueWithKey(K key) {
		return (hash, v) -> hash.put(key, v);
	}

	public static <T> BiConsumer<Collection<T>, T> add() {
		return (l, v) -> l.add(v);
	}
	
}
