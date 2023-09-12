package org.dotspace.oofp.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.type.TypeReference;

public class TypeReflections {

	public static boolean isGenericType(Type type, Type rawType, 
			List<TypeReference<?>> argumentTypeRefs) {
		if (!(type instanceof ParameterizedType) || null == rawType) {
			return false;
		}

		ParameterizedType pt = (ParameterizedType) type;
		if (!rawType.equals(pt.getRawType())) {
			return false;
		}

		if (null == argumentTypeRefs || 
				argumentTypeRefs.size() != pt.getActualTypeArguments().length) {
			return false;
		}

		for (int i = 0; i < argumentTypeRefs.size(); i++) {
			Type at = Optional.ofNullable(argumentTypeRefs.get(i))
					.map(r -> r.getType())
					.orElse(null);
			if (null == at || !at.equals(pt.getActualTypeArguments()[i])) {
				return false;
			}
		}

		return true;
	}

	public static Class<?> getRawClazz(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type rawType = pt.getRawType();
			if (rawType instanceof Class<?>) {
				return (Class<?>) rawType;
			}

			return getRawClazz(rawType);
		}

		if (type instanceof Class<?>) {
			return (Class<?>) type;
		}

		return null;
	}

	
	public static Optional<Pair<Type, List<Type>>> parseParameterizedType(Type type) {
		if (!(type instanceof ParameterizedType)) {
			return Optional.empty();
		}
		
		ParameterizedType pt = (ParameterizedType) type;
		Type rawType = pt.getRawType();
		List<Type> parameterTypes = Arrays.asList(pt.getActualTypeArguments());
		
		return Optional.ofNullable(Pair.of(rawType, parameterTypes));
	}
}
