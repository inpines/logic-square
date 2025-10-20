package org.dotspace.oofp.utils.functional;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dotspace.oofp.utils.functional.monad.Maybe;

@UtilityClass
public class Casters {

	private static final Log logger = LogFactory.getLog(Casters.class);

	public <K, V> Function<Object, Map<K, V>> forMap(
			final Class<K> keyClazz, final Class<V> valueClazz) {
		return instance -> {
				logger.debug(String.format("cast for Map of %s key by %s", 
								valueClazz.getCanonicalName(), keyClazz.getCanonicalName()));
				
				@SuppressWarnings("unchecked")
				Map<K, V> result = (Map<K, V>) instance;
				
				return result;
				
		};
	}

	public Function<Object, String> forText() {
		
		return String.class::cast;
		
	}

	public Function<Object, List<Map<String, Object>>> forListOfResultMap() {
		
		return instance -> {
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = (List<Map<String, Object>>) instance;

			return result;
		};
		
	}

	public <T> Function<Type, Class<T>> castTypeAsClazz() {
		return type -> {
			if (!(type instanceof Class)) {
				return null;
			}
			
			@SuppressWarnings("unchecked")
			Class<T> typeAsClz = (Class<T>) type;
			
			return typeAsClz;
		};
	}

	public <T> Function<Object, T> cast() {
			return value -> {
				try {
					@SuppressWarnings("unchecked")
					T result = (T) value;
					return result;
				} catch (Exception e) {
					return null;
				}
		};
	}

	public <T> Function<Object, T> cast(@NonNull Class<T> clazz) {
		return instance -> {
			if (Maybe.given(instance)
					.filter(inst -> clazz.isAssignableFrom(inst.getClass()))
					.isEmpty()) {
				return null;
			}

			return clazz.cast(instance);
		};
	}

	public <T> Function<Object, List<T>> forList() {
		return instance -> {
			
			@SuppressWarnings("unchecked")
			List<T> result = (List<T>) instance;
			
			return result;

		};
	}

	public <T> Function<Object, Optional<Class<T>>> forClazz() {
		return instance -> Optional.ofNullable(instance)
				.map(obj -> {
					@SuppressWarnings("unchecked")
					Class<T> clz = (Class<T>) obj.getClass();

					return clz;
				});
	}
}
