package org.dotspace.oofp.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.util.functional.BiConsumers;
import org.dotspace.oofp.util.functional.Casters;
import org.dotspace.oofp.util.functional.Suppliers;
import org.dotspace.oofp.util.functional.collector.ClazzMappingCollector;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;

public class TypeConversions {
	
	private static Map<String, String> datetimeFormats = GeneralBuilders
			.of(Suppliers.newHashMap(String.class, String.class))
			.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
					"\\d{4}-\\d{2}-\\d{2}", String.class), "yyyy-MM-dd"))
			.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
					"\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} z", String.class), 
					"yyyy-MM-dd HH:mm:ss z"))
			.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
					"\\d{4}/\\d{2}/\\d{2}", String.class), "yyyy/MM/dd"))
			.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
					"\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2} z", String.class), 
					"yyyy/MM/dd HH:mm:ss z"))
			.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
					"\\d{4}\\d{2}\\d{2}", String.class), "yyyyMMdd"))
			.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
					"\\d{4}\\d{2}\\d{2}\\d{2}\\d{2}\\d{2}z", String.class), "yyyyMMddHHmmssz"))
			.build();
	
	public static <T> T convert(
			Supplier<T> constructor, Map<String, Object> propertyValues) {
		return GeneralBuilders.of(constructor)
				.with(GeneralBuildingWriters.setForEach((t, e) -> assignField(t, e), 
						propertyValues.entrySet()))
				.build();
	}
	
	private static <T> void assignField(T instance, Entry<String, Object> propertyEntry) {
		Optional<Pair<Field, Object>> fieldContext = convertFieldContent(
				instance.getClass(), propertyEntry);
		fieldContext.ifPresent(fldcnt -> {
			ReflectionUtils.setField(fldcnt.getKey(), instance, fldcnt.getValue());
		});
	}

	public static <A, T> T convert(
			Function<A, T> constructor, A args, Map<String, Object> propertyValues) {
		return GeneralBuilders.of(constructor)
				.with(GeneralBuildingWriters.setForEach((t, e) -> assignField(t, e), 
						propertyValues.entrySet()))
				.build(args);
	}
	
	public static <T> T convertToClazz(
			Class<T> resultClazz, Map<String, Object> propertyValues) {
		return propertyValues.entrySet().stream()
				.collect(new ClazzMappingCollector<>(resultClazz));
	}

	public static <T, A, R> R convert(Class<T> resultParameterClazz, 
			List<Map<String, Object>> listOfPropertyValues, 
			Collector<T, A, R> collector) {
		return listOfPropertyValues.stream()
				.map(m -> convertToClazz(resultParameterClazz, m))
				.collect(collector);
	}

	public static <T, A, R> R convertCollectionAsType(Type collectionType, List<Object> values, 
			Collector<T, A, R> collector) {
		
		ParameterizedType pt = (ParameterizedType) collectionType;
		
		Type rawType = pt.getRawType();
		
		if (!(rawType instanceof Class)) {
			return cast(values);
		}
		
		Class<?> rawClazz = (Class<?>) rawType;
		if (!Collection.class.isAssignableFrom(rawClazz)) {
			return cast(values);
		}
		
		Type[] actualTypeArgs = pt.getActualTypeArguments();
		
		@SuppressWarnings("unchecked")
		Collector<? super Object, A, R> clctr = (Collector<? super Object, A, R>) collector;
		
		R result = values.stream()
				.map(value -> {
					Type type = actualTypeArgs[0];
					
					if (Collection.class.isAssignableFrom(
							TypeReflections.getRawClazz(type))) {
						return convertListOfType(type, value);
					}
					return Optional.ofNullable(type)
							.filter(t -> t instanceof Class)
							.map(clz -> {
								@SuppressWarnings("unchecked")
								Class<T> tAsClz = (Class<T>) clz;
								
								return tAsClz;
							})
							.map(clz -> {
								try {
									if (!(value instanceof Map)) {
										return value;
									}
									
									@SuppressWarnings("unchecked")
									Map<String, Object> mapValue = (Map<String, Object>) value;
									
									return convertToClazz(clz, mapValue);
								} catch (Throwable e) {
									return null;
								}
							})
							.orElse(null);
				})
				.collect(clctr);
		
//		@SuppressWarnings("unchecked")
//		Class<T> actualTypeArgAsClz = (Class<T>) actualTypeArgs[0];
//
//		try {
//			result = convertCollectionOfClazz(
//					actualTypeArgAsClz, collectionOfPropertyValues, collector);
//		}
//		catch (Throwable ex) {
//			result = collectionOfPropertyValues;
//		}
		return result;
	}

	private static <T, A, R>  Object convertListOfType(Type type, Object propValue) {
		if (TypeReflections.isGenericType(type, List.class, Arrays.asList(
				new TypeReference<Map<String, Object>>(){}))) {
			return propValue;
		}
		
		Optional<Pair<Type, List<Type>>> typeInfo = TypeReflections
				.parseParameterizedType(type);
		Type tElmt = typeInfo.map(Pair::getRight).map(l -> l.get(0)).orElse(null);
		
		if (!List.class.isAssignableFrom(TypeReflections.getRawClazz(tElmt))) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> props = (List<Map<String, Object>>) propValue;
			
			@SuppressWarnings("unchecked")
			Class<T> tElmtClz = (Class<T>) tElmt;
			
			try {
				return convertCollectionOfClazz(tElmtClz, props, getCollector(type));
			} catch (Throwable e) {
				return null;
			}
		}
		
		Collector<T, A, R> tElmtCollector = getCollector(tElmt);
		return null == tElmtCollector ? null : 
			convertCollectionAsType(tElmt, Optional.ofNullable(propValue)
					.map(Casters.forList(Object.class))
					.orElse(Collections.emptyList()), tElmtCollector);
	}

	private static <R> R cast(List<Object> collectionOfPropertyValues) {
		@SuppressWarnings("unchecked")
		R result = (R) collectionOfPropertyValues;
		
		return result;
	}

	private static <T, A, R> Collector<T, A, R> getCollector(Type type) {
		Optional<Pair<Type, List<Type>>> typeInfo = TypeReflections.parseParameterizedType(type);
		
		Optional<Class<?>> collectionClzOpt = typeInfo.map(p -> p.getLeft())
				.map(t -> (Class<?>) t);
		
		if (collectionClzOpt
				.filter(clz -> List.class.isAssignableFrom(clz))
				.isPresent()) {
			
			@SuppressWarnings("unchecked")
			Collector<T, A, R> listCollector = (Collector<T, A, R>) Collectors.toList();
			
			return listCollector;
		}
		else if(collectionClzOpt
				.filter(clz -> Set.class.isAssignableFrom(clz))
				.isPresent()) {
			
			@SuppressWarnings("unchecked")
			Collector<T, A, R> setCollector = (Collector<T, A, R>) Collectors.toSet();
			
			return setCollector;
		}
		else {
			return null;
		}
	}
	
	public static <T, A, R> R convertCollectionOfClazz(Class<T> clazz,
			List<Map<String, Object>> propertyValues, 
			Collector<T, A, R> collector) throws Throwable {
		return convertToCollection(clazz, propertyValues, 
				collector);
	}
	
	private static <T, A, R> R convertToCollection(Class<T> actualTypeArgAsClz, 
			List<Map<String, Object>> collectionOfPropertyValues,
			Collector<T, A, R> collector) throws Throwable {
		R result = TypeConversions.convert(
					actualTypeArgAsClz, collectionOfPropertyValues, collector);
		return result;
	}

	public static Object parseNumber(String text, Type numberType) {
		Object result;
		
		if (Long.class.equals(numberType)) {
			result = Long.parseLong(text);
		}
		else if (Integer.class.equals(numberType)) {
			result = Integer.parseInt(text);
		}
		else if (Float.class.equals(numberType)) {
			result = Float.parseFloat(text);
		}
		else if (Double.class.equals(numberType)) {
			result = Double.parseDouble(text);
		}
		else if (Byte.class.equals(numberType)) {
			result = Byte.parseByte(text);
		}
		else if (Short.class.equals(numberType)) {
			result = Short.parseShort(text);
		}
		else if (BigDecimal.class.equals(numberType)) {
			result = new BigDecimal(text);
		}
		else {
			result = null;
		}
		
		return result;
		
	}
	
	public static Object convertValue(Number number, Type numberType) {
		Object result;
		
		if (Long.class.equals(numberType)) {
			result = number.longValue();
		}
		else if (Integer.class.equals(numberType)) {
			result = number.intValue();
		}
		else if (Float.class.equals(numberType)) {
			result = number.floatValue();
		}
		else if (Double.class.equals(numberType)) {
			result = number.doubleValue();
		}
		else if (Byte.class.equals(numberType)) {
			result = number.byteValue();
		}
		else if (Short.class.equals(numberType)) {
			result = number.shortValue();
		}
		else if (BigDecimal.class.equals(numberType)) {
			result = new BigDecimal(number.toString());
		}
		else {
			result = null;
		}
		
		return result;
	}

	public static Optional<Pair<Field, Object>> convertFieldContent(Class<?> clazz,
			Entry<String, Object> fieldEntry) {
		
		String fieldName = fieldEntry.getKey();
		Field field = Optional.ofNullable(fieldName)
				.map(fldnm -> ReflectionUtils.findField(clazz, fldnm))
				.orElseGet(() -> {
					if (Character.isUpperCase(fieldName.charAt(0))) {
						String fldnm = fieldName.substring(0, 1).toLowerCase()
								.concat(fieldName.length() > 1 ? fieldName.substring(1) : StringUtils.EMPTY);
						return ReflectionUtils.findField(clazz, fldnm);
					}
					
					return null;
				});
		
		Object value =  fieldEntry.getValue();
		if (null == field && Optional.ofNullable(value)
				.filter(vl -> Boolean.class.isAssignableFrom(
						vl.getClass())).isPresent() && 
				null != fieldName && fieldName.length() > 0) {
			field = ReflectionUtils.findField(clazz, 
					"is".concat(fieldName.substring(0, 1).toUpperCase())
					.concat(fieldName.length() > 1 ? fieldName.substring(1) : StringUtils.EMPTY));
		}
		
		if (null == field || null == value) {
			return Optional.empty();
		}
		
		Class<?> fieldTypeClazz = field.getType();
		if (value instanceof Map) {
			if (!Map.class.isAssignableFrom(fieldTypeClazz)) {
				@SuppressWarnings("unchecked")
				Map<String, Object> valueAsMap = (Map<String, Object>) value;
				
				value = TypeConversions.convertToClazz(fieldTypeClazz, valueAsMap);				
			}
			else {
				Optional<Pair<Type, List<Type>>> typeInfo = TypeReflections.parseParameterizedType(field.getGenericType());
				Type keyType = typeInfo.map(Pair::getRight).map(l -> l.get(0)).orElse(null);
				value = Optional.ofNullable(value)
						.map(Casters.forMap(String.class, Object.class))
						.map(m -> m.entrySet().stream()
							.map(e -> Number.class.isAssignableFrom((Class<?>) keyType) ? 
									Pair.of(TypeConversions.parseNumber(e.getKey(), keyType), e.getValue()) : Pair.of(e.getKey(), e.getValue())
							)
							.collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
						).orElse(Collections.emptyMap());
			}
		}
		else if (value instanceof List) {
			List<Object> listValue = Optional.ofNullable(value)
					.map(Casters.forList(Object.class))
					.orElse(Collections.emptyList());
			if (List.class.isAssignableFrom(field.getType())) {
				value = getListFieldValue(field, listValue);
			}
			else if (Set.class.isAssignableFrom(field.getType())) {
				value = getSetFieldValue(field, listValue);
			}
			else {
				throw new UnsupportedOperationException("不支援的collection type!!");
			}
		}
//		else if (value instanceof Set) {
//			Type fieldGenericType = field.getGenericType();
//			if (TypeReflections.isGenericType(fieldGenericType, Set.class, 
//					Arrays.asList(new TypeReference<Map<String, Object>>() {}))) {
//				return Optional.ofNullable(Pair.of(field, value));
//			}
//
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> valueAsListOfMap = (List<Map<String, Object>>) value;
//			
//			value = TypeConvertsions.convertCollectionOfType(
//					fieldGenericType, valueAsListOfMap, Collectors.toSet());
//		}
		else if (value instanceof Number) {
			if (Date.class.isAssignableFrom(fieldTypeClazz)) {
				Long millis = Number.class.cast(value).longValue();
				value = TypeConversions.parseFromTimeMillis(millis);
			}
			else {
				value = TypeConversions.convertValue((Number) value, 
						field.getGenericType());
			}
		}
		else if (value instanceof String) {
			
			Optional<String> textValueOpt = Optional.ofNullable(value)
					.map(Casters.forText());
			
			if (Date.class.isAssignableFrom(fieldTypeClazz)) {
				value = textValueOpt
						.map(s -> Pair.of(s, determineDatetimeFormat(s)))
						.map(e -> formatDate(e))
						.orElse(null);				
			}
			else if (Class.class.isAssignableFrom(fieldTypeClazz)) {
				value = textValueOpt
						.map(s -> convertClazzValue(s))
						.orElse(null);
			}
		}
		
		if (field.getModifiers() == Modifier.STATIC) {
			return Optional.empty();
		}

		if (fieldTypeClazz.isEnum() && value instanceof String) {
			value = getEnumValue(field.getType(), (String) value);
		}
		else if (fieldTypeClazz.isArray()) {
			Class<?> eClz = fieldTypeClazz.getComponentType();
			
			@SuppressWarnings("unchecked")
			Class<Object[]> arrayClazz = (Class<Object[]>) fieldTypeClazz;
			
			value = convertArray(parseArrayValue(value, arrayClazz), eClz);
			
		}
		
		return Optional.ofNullable(Pair.of(field, value));
	}

	private static Class<?> convertClazzValue(String s) {
		try {
			return Class.forName(s);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static Date formatDate(Pair<String, Optional<String>> entry) {
		return entry.getRight()
				.map(fmt -> parseDate(entry.getLeft(), fmt))
				.orElse(null);
	}

	private static Date parseDate(String dtText, String format) {
		try {
			return new SimpleDateFormat(format).parse(dtText);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Optional<String> determineDatetimeFormat(String datetimeText) {
		if (StringUtils.isBlank(datetimeText)) {
			return Optional.empty();
		}
						
		for (Entry<String, String> e: datetimeFormats.entrySet()) {
			if (datetimeText.matches(e.getKey())) {
				return Optional.ofNullable(e.getValue());
			}
		}	
		
		return Optional.empty();
	}
	
	public static Object convertArray(Object value, Class<?> clazz) {
		
		return (value instanceof byte[]) ? getBytes((byte[]) value) : 
			getObjectArray((Object []) value, clazz);
		
	}

	public static Object parseArrayValue(Object value, Class<Object[]> arrayClazz) {
		
		if (value instanceof String) {
			return Base64.decodeBase64((String) value);
		}
		
		return value;
	}
	
	private static Object[] getObjectArray(Object[] array, Class<?> clazz) {
		Object[] result = (Object[]) Array.newInstance(clazz, array.length);
		
		for (int i = 0; i < array.length; i++) {
			
			if (array[i] instanceof Map && !Map.class.isAssignableFrom(clazz)) {
				
				@SuppressWarnings("unchecked")
				Map<String, Object> valueAsMap = (Map<String, Object>) array[i];
				
				Array.set(result, i, TypeConversions.convertToClazz(clazz, valueAsMap));
				continue;
			}
			Array.set(result, i, array[i]);
		}
		
		return result;
	}

	private static byte[] getBytes(byte[] bytes) {
		return bytes;
	}

	public static Date parseFromLocalDatetime(Long ldtValue) {
		LocalDateTime ldt = LocalDateTime.parse(
                String.format("%d", ldtValue), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static Date parseFromTimeMillis(Long millisecond) {
		return new Date(millisecond);
	}
	
	private static <T extends Enum<T>> T getEnumValue(Class<?> fieldClazz, String value) {
		if (!Enum.class.isAssignableFrom(fieldClazz)) {
			return null;
		}
		
		@SuppressWarnings("unchecked")
		Class<T> enumClazz = (Class<T>) fieldClazz;
		
		return Enum.valueOf((Class<T>) enumClazz, value);
	}
	
	private static Object getSetFieldValue(Field field, List<Object> value) {
		Type fieldGenericType = field.getGenericType();
		if (TypeReflections.isGenericType(fieldGenericType, Set.class, 
				Arrays.asList(new TypeReference<Map<String, Object>>() {}))) {
			return Optional.ofNullable(value).map(Casters.forListOfResultMap())
					.map(TypeConversions::toSet)
					.orElse(Collections.emptySet());
		}
		
//		@SuppressWarnings("unchecked")
//		List<Map<String, Object>> valueAsListOfMap = (List<Map<String, Object>>)value;
		
		Set<Object> result = TypeConversions.convertCollectionAsType(
				fieldGenericType, value, Collectors.toSet());
		return result;
	}

	private static <T> Set<T> toSet(Collection<T> collection) {
		return collection.stream().collect(Collectors.toSet());
	}

	private static Object getListFieldValue(Field field, List<Object> value) {
		Type fieldGenericType = field.getGenericType();
		if (TypeReflections.isGenericType(fieldGenericType, List.class, 
				Arrays.asList(new TypeReference<Map<String, Object>>() {}))) {
			return value;
		}

//		@SuppressWarnings("unchecked")
//		List<Map<String, Object>> valueAsListOfMap = (List<Map<String, Object>>)value;
		
		List<Object> result = TypeConversions.convertCollectionAsType(
				fieldGenericType, value, Collectors.toList());
		return result;
	}
	
}
