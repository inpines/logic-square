package org.dotspace.creation.constructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Constructors {

	public static <K, V> Supplier<Map<K, V>> forHashMap(Class<K> kclz, Class<V> vclz) {
		return () -> new HashMap<K,V>();
	}

}
