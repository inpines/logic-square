package org.dotspace.creation;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MemberAccessors {

	public static <K, V> BiConsumer<Map<K, V>, V> put(K key) {
		return (hash, v) -> hash.put(key, v);
	}

	public static <V> BiConsumer<List<V>, V> add() {
		return (l, v) -> l.add(v);
	}
	
}
