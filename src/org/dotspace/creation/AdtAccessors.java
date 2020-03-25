package org.dotspace.creation;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AdtAccessors {

	public static <K, V> BiConsumer<Map<K, V>, V> forMapToPutByKeyOf(
			K key, Class<V> clazz) {
		return (hash, v) -> hash.put(key, v);
	}

	public static <V> BiConsumer<List<V>, V> forListToAdd(Class<V> clazz) {
		return (l, v) -> l.add(v);
	}
	
}
