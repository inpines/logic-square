package org.dotspace.v1.creation;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

public class AdtAccessors {

	public static <K, V> BiConsumer<Map<K, V>, V> forMapToPutByKeyOf(
			K key, Class<V> clazz) {
		return (hash, v) -> hash.put(key, v);
	}

	public static <V> BiConsumer<Collection<V>, V> forAddition(Class<V> clazz) {
		return (l, v) -> l.add(v);
	}
	
}
