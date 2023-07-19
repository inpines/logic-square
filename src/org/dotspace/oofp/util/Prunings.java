package org.dotspace.oofp.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.util.functional.Casters;

public class Prunings {

	public static <T> T nullize(T instance, Set<String> associatedClazzes) {
		Class<T> clazz = Optional.ofNullable(instance)
				.map(Prunings::getClazz)
				.orElse(null);
		
		if (null == clazz || null == instance || !isDerivative(clazz)) {
			return backward(instance, clazz, associatedClazzes);
		}
		
		class Context {
			List<SimplePair<Object, Object>> results = new ArrayList<>();
			
			void removeNull(Set<?> instance) {
				results.stream().filter(entry -> null == entry.getValue())
				.forEach(entry -> instance.remove(entry.getKey()));			
			}
			
			void removeNull(List<?> instance) {
				results.stream().filter(entry -> null == entry.getValue())
				.forEach(entry -> instance.remove(((Integer)entry.getKey()).intValue()));							
			}
			
			void removeNull(Map<?, ?> instance) {
				results.stream().filter(entry -> null == entry.getValue())
				.forEach(entry -> instance.remove(entry.getKey()));
			}
			
			<E> E[] copyNotNullArray(E[] instance) {
				@SuppressWarnings("unchecked")
				E[] result = (E[]) new Object[instance.length];
				
				for (int i = 0;i < instance.length; i++) {
					@SuppressWarnings("unchecked")
					E value = (E) Array.get(instance, i);
					
					result[i] = value;
				}
				return result;
			}
			
			<E> Collection<E> copyNotNull(Collection<E> instance) {
				Class<?> collectionClazz = instance.getClass();
				
				try {
					@SuppressWarnings("unchecked")
					Collection<E> result = (Collection<E>) collectionClazz.newInstance();
					
					Iterator<SimplePair<Object, Object>> itr = results.iterator();
					for (;itr.hasNext();) {
						SimplePair<Object, Object> entry = itr.next();
						if (null == entry.getValue()) {
							continue;
						}
						@SuppressWarnings("unchecked")
						E value = (E) entry.getValue();
						result.add(value);
					}
					return result;
				} catch (Throwable e) {
					return null;
				}
				
			}

		}
		
		Context ctx = new Context();
		getRepeativeInstances(instance).forEach(entry -> {
			Object index = entry.getKey();
			Object inst = entry.getValue();
			
			Class<?> clz = inst.getClass();
			Optional.ofNullable(clz).ifPresent(c -> associatedClazzes.add(c.getCanonicalName()));

			List<Field> fields = getFields(clz);
			List<SimplePair<Field, Object>> fldValues = fields.stream()
					.map(fld -> SimplePair.of(fld, getFieldValue(inst, fld)))
					.filter(fldval -> null != fldval.getRight())
					.filter(fldval -> !associatedClazzes.contains(Optional.ofNullable(fldval.getRight())
							.map(Prunings::getClazz)
							.map(Class::getCanonicalName)
							.orElse(StringUtils.EMPTY)))
					.map(fldval -> {
						return SimplePair.of(fldval.getLeft(), 
								Prunings.nullize(fldval.getRight(), associatedClazzes));
					})
					.collect(Collectors.toList());
			
			List<Throwable> exceptions = new ArrayList<>();
			
			fldValues.forEach(fv -> {
				if (null == fv.getRight()) {
					setFieldValue(inst, fv.getLeft(), fv.getRight()).ifPresent(exceptions::add);
				}
			});
			
			if (!exceptions.isEmpty()) {
				throw new RuntimeException(exceptions.stream().findFirst().orElse(null));
			}

			ctx.results.add(SimplePair.of(index, Optional.ofNullable(fldValues)
					.filter(fldVls -> !fldVls.isEmpty() && !fldValues.stream()
							.allMatch(fv -> null == fv.getRight()))
					.map(fldVls -> inst)
					.orElse(null)));
			
		});
		
		if (!Map.class.isAssignableFrom(clazz) && !Collection.class.isAssignableFrom(clazz) && !clazz.isArray()) {
			@SuppressWarnings("unchecked")
			T result = (T) ctx.results.get(0).getValue();
			
			return backward(result, clazz, associatedClazzes);
		}
		
		if (instance instanceof Set) {
			ctx.removeNull((Set<?>) instance);
		}
		else if (instance instanceof List) {
			ctx.removeNull((List<?>) instance);
		}
		else if (instance instanceof Map) {
			ctx.removeNull((Map<?, ?>) instance);
		}
		else if (clazz.isArray()) {
			Object[] copied = ctx.copyNotNullArray((Object[]) instance);
			
			@SuppressWarnings("unchecked")
			T result = (T) copied;
			
			return backward(result, clazz, associatedClazzes);
		}
		else {
			Collection<?> collection = Optional.ofNullable(instance)
					.map(Casters.cast(Collection.class))
					.orElse(null);

			Collection<?> copied = ctx.copyNotNull(collection);
			
			@SuppressWarnings("unchecked")
			T result = (T) copied;
			
			return backward(result, clazz, associatedClazzes);
		}		
		
		return backward(instance, clazz, associatedClazzes);
	}

	private static <T> T backward(T instance, Class<T> clazz, Set<String> associatedClazzes) {
		Optional.ofNullable(clazz).ifPresent(clz -> associatedClazzes.remove(clz.getCanonicalName()));
		
		return instance;
	}
	
	private static Stream<SimplePair<Object, Object>> getRepeativeInstances(Object instance) {
		Class<?> clazz = instance.getClass();
		
		if (!Collection.class.isAssignableFrom(clazz) && !Map.class.isAssignableFrom(clazz) && !clazz.isArray()) {
			return Stream.of(SimplePair.of(StringUtils.EMPTY, instance));
		}
		
		if (instance instanceof Set) {
			Stream<SimplePair<Object, Object>> result = ((Set<?>) instance).stream()
					.map(inst -> SimplePair.of(inst, inst));
			
			return result;
		}

		if (instance instanceof Collection) {
			Builder<SimplePair<Object, Object>> builder = Stream.builder();
			Iterator<?> itr = ((Collection<?>) instance).iterator();
			for (int i = 0; itr.hasNext(); i++) {
				Object inst = itr.next();
				builder.add(SimplePair.of(i, inst));
			}
			return builder.build();
		}
		
		if (instance instanceof Map) {
			return ((Map<?, ?>) instance).entrySet().stream()
					.map(e -> SimplePair.of(e.getKey(), e.getValue()));
		}
		
		
		Stream<SimplePair<Object, Object>> result = Optional.ofNullable(instance)
				.filter(inst -> inst.getClass().isArray() && !(inst instanceof char[]))
				.map(clz -> {
					Object[] elements = (Object []) instance;
					
					Builder<SimplePair<Object, Object>> builder = Stream.builder();
					for (int i = 0; i < elements.length; i ++) {
						builder.add(SimplePair.of(i, elements[i]));
					}
					return builder.build();
				}).orElse(Stream.empty());
		
		return result;
	}

	private static <T>  Class<T> getClazz(T instance) {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) instance.getClass();
		
		return clazz;
	}
	
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		
		if (isDerivative(clazz)) {
			Optional.ofNullable(clazz.getSuperclass())
			.ifPresent(superClazz -> fields.addAll(getFields(superClazz)));
		}
		
//		Class<?> superClazz = clazz.getSuperclass();
//		while (null != superClazz && !(superClazz instanceof Object)) {
//			fields.addAll(getFields(superClazz));
//		}
		
		Arrays.asList(clazz.getDeclaredFields()).stream()
		.filter(fld -> {
			int fldmdfrs = fld.getModifiers();
			return !Modifier.isStatic(fldmdfrs) && !Modifier.isFinal(fldmdfrs) && !fld.isSynthetic();
		})
		.forEach(fields::add);
		
//		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		
		return fields;
	}
	
	private static boolean isDerivative(Class<?> clazz) {
		if (CharSequence.class.isAssignableFrom(clazz) || clazz.isPrimitive() || clazz.isEnum() || clazz.isArray() ||
				Number.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz) ||
				Date.class.isAssignableFrom(clazz) || isObjectClazz(clazz)) {
			return false;
		}
		return true;
	}

	private static boolean isObjectClazz(Class<?> clazz) {
		return Optional.ofNullable(clazz)
				.map(Class::getCanonicalName)
				.filter(clznm -> clznm.equals(Object.class.getCanonicalName()))
				.isPresent();
	}
	
	public static Object getFieldValue(Object instance, Field field) {
		boolean accessible = field.isAccessible();
		
		Object result;
		try {
			field.setAccessible(true);
			result = field.get(instance);
		}
		catch (Throwable ex) {
			result = null;
		}
		finally {
			field.setAccessible(accessible);
		}
		
		return result;
	}
	
	public static Optional<Throwable> setFieldValue(Object instance, Field field, Object value) {
		
		Optional<Throwable> result = Optional.empty();
		
		boolean accessible = field.isAccessible();
		try {
			field.setAccessible(true);
			field.set(instance, value);
		}
		catch (Throwable ex) {
			result = Optional.ofNullable(ex);
		}
		finally {
			field.setAccessible(accessible);
		}
		
		return result;
	}

}
