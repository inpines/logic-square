package org.dotspace.oofp.utils.functional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.functional.monad.Maybe;

import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@UtilityClass
public class Casters {

	private static final Log logger = LogFactory.getLog(Casters.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

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
				@SuppressWarnings("unchecked")
				T result = (T) value;

				return result;
		};
	}

	/**
	 * ✅ 統一入口：
	 * - type 是 Class -> 走純 cast
	 * - type 是 ParameterizedType / 其他複合 Type -> 走 ObjectMapper convert
	 */
	@SuppressWarnings("unchecked")
	public <T> Function<Object, T> cast(@NonNull Type type) {
		if (type instanceof Class<?> rawClass) {
            return (Function<Object, T>) cast((Class<?>) rawClass);
		}

		// ParameterizedType / GenericArrayType / WildcardType / TypeVariable ... 全部交給 Jackson
		JavaType javaType = objectMapper.getTypeFactory().constructType(type);
		return instance -> {
			if (instance == null) {
				throw new IllegalArgumentException("Cannot cast null instance to type: " + type.getTypeName());
			}

			// 若 instance 本身已經符合目標 raw type，可選擇直接回傳以避免 convert 成本
			Class<?> raw = javaType.getRawClass();
			if (raw != null && raw.isInstance(instance) && !(type instanceof ParameterizedType)) {
				return (T) instance;
			}

			return objectMapper.convertValue(instance, javaType);
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

	public <T> Function<Object, T> cast(@NonNull TypeReference<T> typeReference) {
		return instance -> objectMapper.convertValue(instance, typeReference);
	}

	public <T> Function<Object, List<T>> forList() {
		return instance -> {
			
			@SuppressWarnings("unchecked")
			List<T> result = (List<T>) instance;
			
			return result;

		};
	}

	public <T> Function<Object, Validation<Violations, T>> castOrInvalid(Type type) {
		JavaType javaType = objectMapper.getTypeFactory().constructType(type);
		return instance -> {
			if (instance == null) {
				return Validation.invalid(Violations.violate("casters.cast.null",
						"cannot cast null instance to type: " + type.getTypeName()));
			}
			try {
				Class<?> raw = javaType.getRawClass();
				if (raw != null && raw.isInstance(instance) && !(type instanceof ParameterizedType)) {
					@SuppressWarnings("unchecked") T t = (T) instance;
					return Validation.valid(t);
				}
				return Validation.valid(objectMapper.convertValue(instance, javaType));
			} catch (IllegalArgumentException | ClassCastException e) {
				return Validation.invalid(Violations.violate("casters.cast.failed",
						"cast failed to type: " + type.getTypeName()
								+ ", error=" + e.getClass().getSimpleName()));
			}
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
