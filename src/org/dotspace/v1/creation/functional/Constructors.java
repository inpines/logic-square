package org.dotspace.v1.creation.functional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Constructors {

	public static <K, V> Supplier<Map<K, V>> forHashMap(Class<K> kclz, Class<V> vclz) {
		return () -> new HashMap<K,V>();
	}

	public static <I> Supplier<List<I>> forArrayList(Class<I> clazz) {
		return () -> new ArrayList<>();
	}

}
