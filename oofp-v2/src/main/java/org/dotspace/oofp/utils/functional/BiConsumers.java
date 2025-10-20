package org.dotspace.oofp.utils.functional;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class BiConsumers {

	public <E> BiConsumer<List<E>, E> forListOf() {

		return List::add;
	}
	
	public <K, V> BiConsumer<Map<K, V>, V> forMapOf(K key) {
		
		return (m, v) -> m.put(key, v);
	}

	public <K> BiConsumer<Map<K, Function<String, String>>, Function<String, String>> forProcessorExecutorsIndexOf(
			K key, UnaryOperator<String> processorExecutor) {
		return (m, pe) -> m.put(key, processorExecutor);
	}

}
