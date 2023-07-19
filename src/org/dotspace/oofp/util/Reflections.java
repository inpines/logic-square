package org.dotspace.oofp.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Reflections {
	
	private static Log logger = LogFactory.getLog(Reflections.class);
	
	private Reflections() {
		super();
	}
	
	public static List<Field> getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		
		Class<?> superClazz = clazz.getSuperclass();
		while (null != superClazz && !(superClazz instanceof Object)) {
			fields.addAll(getFields(superClazz));
		}
		
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		
		return fields;
	}
	
	public static <T> Object getFieldValue(T obj, Field field) {
		FieldAccessors fa = FieldAccessors.forInstance(obj);
		
		return fa.read(field);
//		class ReadingConsumer {
//			
//			private Object instance;
//			
//			private Object result;
//			
//			public ReadingConsumer(Object instance) {
//				this.instance = instance;
//			}
//			
//			public void read(Field fld) {
//				try {
//					result = fld.get(instance);
//				} catch (Exception e) {
//					result = null;
//				}
//			}
//			
//			public Object getResult() {
//				return result;
//			}
//		}
//		
//		ReadingConsumer readingConsumer = new ReadingConsumer(obj);
//		access(field, readingConsumer::read);
//		
//		return readingConsumer.getResult();
		/*
		Object value = null;
		boolean inaccessibale = !field.isAccessible();
		
		try {
			if (inaccessibale) {
				field.setAccessible(true);
			}
			value = field.get(obj);

		} catch (Exception ex) {
			
		} finally {
			if (inaccessibale) {
				field.setAccessible(false);
			}
		}
		
		return value;
		*/
	}
	
//	private static void access(Field field, Consumer<Field> consumer) {
//		
//		boolean inaccessibale = !field.isAccessible();
//		
//		try {
//			if (inaccessibale) {
//				field.setAccessible(true);
//			}
//			consumer.accept(field);
//		} catch (Exception ex) {
//			logger.debug("error on accessing field : " + field, ex);
//		} finally {
//			if (inaccessibale) {
//				field.setAccessible(false);
//			}
//		}
//	}

	public static <T> T construct(Class<T> clazz) {
		try {
//			return clazz.newInstance();
			return Optional.ofNullable(clazz.getConstructor())
					.map(Reflections::construct)
					.orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	private static <T> T construct(Constructor<T> constructor) {
		try {
			return constructor.newInstance();
		} catch (Throwable e) {
			return null;
		}
	}
		
	public static <T> void setFieldValue(T target, String name, Object value) {
		logger.debug(String.format("set field %s = %s", name, value));
		
		Field fld = getField(target.getClass(), name);
//		
//		class WritingConsumer {
//
//			private Object val;
//			
//			public WritingConsumer(Object val) {
//				this.val = val;
//			}
//			
//			public void write(Field field) {
//				try {
//					field.set(target, val);
//				} catch (Exception e) {
//					logger.debug("error on set field value : " + field, e);
//				}
//			}
//		}
//
//		WritingConsumer writingConsumer = new WritingConsumer(value);
//		access(fld, writingConsumer::write);
		FieldAccessors fa = FieldAccessors.forInstance(target);
		fa.write(fld, value);
	}

	public static <T> Field getField(Class<T> clazz, String name) {
		Field fld = getDeclaredField(clazz, name);
		
		if (null != fld) {
			return fld;
		}
		
		Class<?> superClazz = clazz.getSuperclass();
		while ((null != superClazz && superClazz instanceof Object)) {
			fld = getDeclaredField(superClazz, name);
			
			if (null != fld) {
				break;
			}
		}
		
		return fld;
	}

	private static <T> Field getDeclaredField(Class<T> clazz, String name) {
		Field fld;
		
		try {
			fld = clazz.getDeclaredField(name);
		} catch (Exception e) {
			fld = null;
		}
		
		return fld;
	}

	public static Object invoke(
			Object obj, String methodName, Object[] args) {
		Class<?> clazz = obj.getClass();
		
		Class<?>[] parameterTypes = Arrays.asList(args)
				.stream()
				.map(o -> o.getClass())
				.collect(Collectors.toList())
				.toArray(new Class<?>[0]);
		
		try {
			Method method = clazz.getMethod(methodName, parameterTypes);
			return method.invoke(obj, args);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isGenericType(Type rawType, Type[] parameterizedTypes, 
			Object instance) {
		List<ParameterizedType> types = Optional.ofNullable(instance)
				.map(inst -> inst.getClass())
				.map(clz -> clz.getGenericInterfaces())
				.map(gis -> Arrays.asList(gis).stream()
						.filter(gi -> gi instanceof ParameterizedType)
						.map(gi -> (ParameterizedType) gi)
						.filter(pt -> rawType.equals(pt.getRawType()))
						.filter(pt -> {
							Type[] atas = pt.getActualTypeArguments();
							if (parameterizedTypes.length != atas.length) {
								return false;
							}
							
							for (int i = 0; i < parameterizedTypes.length; i ++) {
								if (!parameterizedTypes[i].equals(atas[i])) {
									return false;
								}
							}
							
							return true;
						})
						.collect(Collectors.toList()))				
				.orElse(Collections.emptyList());
		
		return !types.isEmpty();
				
	}

}
