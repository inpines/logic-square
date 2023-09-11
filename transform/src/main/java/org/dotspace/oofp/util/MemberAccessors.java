package org.dotspace.oofp.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

public class MemberAccessors {

	private static final String METHOD_TRAIL_PATTERN = "\\(\\s*\\)$";

	private static Log logger = LogFactory.getLog(MemberAccessors.class);
	
	private Object instance;
	
	public static MemberAccessors forInstance(Object instance) {
		return new MemberAccessors(instance);
	}

	public static MemberAccessors forInstance(Object instance, String path) {
		MemberAccessors mas = MemberAccessors.forInstance(instance);

		if (StringUtils.isBlank(path)) {
			return mas;
		}
		
		List<String> fieldNames = Arrays.asList(path.split("\\."));
		if (fieldNames.isEmpty()) {
			fieldNames.add(path);
		}
				
		Object inst = instance;
		Class<?> clazz = inst.getClass();
		
		for (int i = 0; i < fieldNames.size(); i ++) {
			Pattern p = Pattern.compile("([^\\[]*)(\\[(\\w+)\\])?");
			Matcher m = p.matcher(fieldNames.get(i));
			if (!m.matches()) {
				throw new RuntimeException(String.format(
						"invalid field path %s", path));
			}

			String fldName = m.group(1);
			String index = m.group(3);

			Field field = ReflectionUtils.findField(clazz, fldName);
			
			inst = mas.read(field);
			clazz = inst.getClass();

			if (null != index) {
				Class<?> fieldType = field.getType();
				inst = (Collection.class.isAssignableFrom(fieldType)) ?
						((Collection<?>) inst).toArray()[Integer.parseInt(index)]:
							((Map.class.isAssignableFrom(fieldType)) ?
									inst = ((Map<?, ?>) inst).get(index) : inst);
			}

			mas = MemberAccessors.forInstance(inst);
		}
		return mas;
	}
	
	protected MemberAccessors(Object instance) {
		this.instance = instance;
	}
	
	public Object invoke(Method method, Object... args) {
		boolean accessible = isAccessible(method);
		
		Object result;
		try {
			if (!accessible) {
				method.setAccessible(true);
			}
			result = method.invoke(instance, args);
		} catch (Exception e) {
			result = null;
		} finally {
			if (!accessible) {
				method.setAccessible(false);
			}
		}
		
		return result;
	}
	
	public Object read(Field fld) {
		boolean accessible = isAccessible(fld);
		
		Object result;
		try {
			if (!accessible) {
				fld.setAccessible(true);
			}
			result = fld.get(instance);
		} catch (Exception e) {
			result = null;
		} finally {
			if (!accessible) {
				fld.setAccessible(false);
			}
		}
		
		return result;
	}
	
	public Object read(String memberName) {
		if (null == memberName) {
			return null;
		}
		
		if (memberName.matches(".*".concat(METHOD_TRAIL_PATTERN))) {
			Method method = ReflectionUtils.findMethod(
					instance.getClass(), memberName.replaceAll(METHOD_TRAIL_PATTERN, StringUtils.EMPTY));
			return invoke(method);
		}
		
		return read(ReflectionUtils.findField(instance.getClass(), memberName));
	}
	
	private boolean isAccessible(AccessibleObject accessibleObject) {
//		return accessibleObject.canAccess(instance);
		return accessibleObject.isAccessible();
	}

	public void write(Field fld, Object value) {
		boolean accessible = isAccessible(fld);
		try {
			if (!accessible) {
				fld.setAccessible(true);
			}
			fld.set(instance, value);
		} catch (Throwable e) {
			logger.debug("error on set field value : " + fld, e);
		} finally {
			if (!accessible) {
				fld.setAccessible(false);
			}
		}
	}
	
	public void write(String memberName, Object value) {		
		if (null == memberName) {
			return;
		}
		
		if (memberName.matches(".*".concat(METHOD_TRAIL_PATTERN))) {
			ReflectionUtils.doWithMethods(value.getClass(), new MethodCallback() {

				@Override
				public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
					try {
						method.invoke(method, value);
					} catch (Throwable e) {
						logger.debug(String.format("error on invoke %s.%s(%s)", 
								method.getDeclaringClass(), method.getName(), value), e);
					}
				}
				
			}, new MethodFilter() {

				@Override
				public boolean matches(Method method) {
					if (null == method) {
						return false;
					}
					
					if (!method.getName().equals(memberName
							.replaceAll(METHOD_TRAIL_PATTERN, StringUtils.EMPTY))) {
						return false;
					}
					
					if (!Optional.ofNullable(value)
							.map(Object::getClass)
							.filter(clz -> clz.isAssignableFrom(method.getParameterTypes()[0])).isPresent()) {
						return false;
					}
							
					return false;
				}
				
			});
		}	
		
		write(ReflectionUtils.findField(instance.getClass(), memberName), value);
	}
	
}
