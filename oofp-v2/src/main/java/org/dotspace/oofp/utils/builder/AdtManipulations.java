package org.dotspace.oofp.utils.builder;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@UtilityClass
public class AdtManipulations {

	public <K, V> Supplier<Map<K, V>> supplyMap() {
		return HashMap::new;
	}
	
	public <T> Supplier<List<T>> supplyList() {
		return ArrayList::new;
	}
	
	public <K, V> BiConsumer<Map<K, V>, V> consumePutting(K key) {
		return (hash, v) -> hash.put(key, v);
	}

	public <T> BiConsumer<Collection<T>, T> consumeAdding() {
		return Collection::add;
	}
	
}
