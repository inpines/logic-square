package org.dotspace.oofp.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.util.ReflectionUtils;

public class Reflections {

    private Reflections() {
		super();
	}
	
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		ReflectionUtils.doWithFields(clazz, fields::add);
		return fields;
	}
	
	public static <T> T construct(Class<T> clazz) {
		try {
			return Optional.of(clazz.getConstructor())
					.map(Reflections::construct)
					.orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	private static <T> T construct(Constructor<T> constructor) {
		try {
			return constructor.newInstance();
		} catch (Exception e) {
			return null;
		}
	}
		
	public static <T> Field getField(Class<T> clazz, String name) {
		return ReflectionUtils.findField(clazz, name);
	}

	public static Object invoke(
			Object obj, String methodName, Object[] args) {
		Class<?> clazz = obj.getClass();
		
		Class<?>[] parameterTypes = Arrays.stream(args)
				.map(Object::getClass)
				.toList()
				.toArray(new Class<?>[0]);
		
		try {
			Method method = clazz.getMethod(methodName, parameterTypes);
			return ReflectionUtils.invokeMethod(method, obj, args);
		} catch (Exception e) {
			return null;
		}
	}

}
