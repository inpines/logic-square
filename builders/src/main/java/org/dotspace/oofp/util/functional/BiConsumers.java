package org.dotspace.oofp.util.functional;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BiConsumers {

	private static final Log logger = LogFactory.getLog(BiConsumers.class);

	private BiConsumers() {
		super();
	}
	
	public static <E> BiConsumer<List<E>, E> forListOf(Class<E> clazz) {
		
		logger.debug(String.format("list of %s", clazz.getCanonicalName()));
		
		return (l, e) -> l.add(e);
	}
	
	public static <K, V> BiConsumer<Map<K, V>, V> forMapOf(K key, Class<V> valueClazz) {
		
		logger.debug(String.format("map of %s, %s", 
				key.getClass().getCanonicalName(), valueClazz.getCanonicalName()));

		return (m, v) -> m.put(key, v);
	}

	public static <K> BiConsumer<Map<K, Function<String, String>>, Function<String, String>> forProcessorExecutorsIndexOf(
			K key, Function<String, String> processorExecutor) {
		return (m, pe) -> m.put(key, processorExecutor);
	}
}
