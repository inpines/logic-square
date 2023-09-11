package org.dotspace.oofp.util.functional;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Casters {

	private static final Log logger = LogFactory.getLog(Casters.class);

	private Casters() {
		super();
	}
	
	public static <V> Function<Object, List<V>> forList(final Class<V> itemClazz) {
		
		return instance -> {
			
			logger.debug(String.format("cast for List of %s", 
					itemClazz.getCanonicalName()));

			@SuppressWarnings("unchecked")
			List<V> result = (List<V>) instance;
			
			return result;

		};
	}

	public static <K, V> Function<Object, Map<K, V>> forMap(
			final Class<K> keyClazz, final Class<V> valueClazz) {
		return instance -> {
				logger.debug(String.format("cast for Map of %s key by %s", 
								valueClazz.getCanonicalName(), keyClazz.getCanonicalName()));
				
				@SuppressWarnings("unchecked")
				Map<K, V> result = (Map<K, V>) instance;
				
				return result;
				
		};
	}

	public static Function<Object, String> forText() {
		
		return  instance -> (String) instance;
		
	}

	public static Function<Object, List<Map<String, Object>>> forListOfResultMap() {
		
		return instance -> {
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = (List<Map<String, Object>>) instance;

			return result;
		};
		
	}

//	public static <T> Function<Object, T> forClazz(final Class<T> clazz) {
//		
//		return clazz::cast;
//		
//	}

	public static <T> Function<Object, T> cast(Class<T> clazz) {
		return value -> {
			@SuppressWarnings("unchecked")
			T result = (T) value;
			return result;
		};
	}

	public static <T> Function<Type, Class<T>> castTypeAsClazz(Class<T> clazz) {
		return type -> {
			if (!(type instanceof Class)) {
				return null;
			}
			
			@SuppressWarnings("unchecked")
			Class<T> typeAsClz = (Class<T>) type;
			
			return typeAsClz;
		};
	}

	public static <T> Function<Object, T> cast() {
		return value -> {
			@SuppressWarnings("unchecked")
			T result = (T) value;
			return result;
		};
	}

}
