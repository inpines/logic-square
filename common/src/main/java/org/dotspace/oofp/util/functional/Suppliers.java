package org.dotspace.oofp.util.functional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Suppliers {

	private static final Log logger = LogFactory.getLog(Suppliers.class);

	private Suppliers() {
		super();
	}
	
	public static <T> Supplier<List<T>> newList(final Class<T> clazz) {
		
		return () -> {

			String clazzName = clazz.getCanonicalName();

			logger.debug(String.format(
					"newList: %1$s -> get List<%1$s> instance by new ArrayList<%1$s>()", 
					clazzName));
			return new ArrayList<>();

		};
	}

	public static <K, V> Supplier<Map<K, List<V>>> newListOfItemsByKey(
			final Class<K> keyClazz, final Class<V> itemsClazz) {
		return () -> {

			String itemsClazzName = itemsClazz.getCanonicalName();
			String keyClazzName = keyClazz.getCanonicalName();
			logger.debug(String.format(
							"newListOfItemsByKey: (%1$s, %2$s) -> get Map<%2$s, "
									+ "List<%1$s>> instance by new HashMap<%2$s, List<%1$s>>()",
							itemsClazzName, keyClazzName));
			
			return new HashMap<>();

		};
	}

	public static <K, V> Supplier<Map<K, V>> newHashMap(Class<K> keyClazz, Class<V> valueClazz) {
		
		return () -> {
			
			String itemsClazzName = valueClazz.getCanonicalName();
			
			String keyClazzName = keyClazz.getCanonicalName();
			
			logger.debug(String.format("newItemByKey: (%1$s, %2$s) -> "
					+ "get Map<%2$s, %1$s> instance by new HashMap<%2$s, %1$s>()",
					itemsClazzName, keyClazzName));
			
			return new HashMap<>();

		};
	}

}
