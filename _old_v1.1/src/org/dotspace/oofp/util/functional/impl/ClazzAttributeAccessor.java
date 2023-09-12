package org.dotspace.oofp.util.functional.impl;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;

public class ClazzAttributeAccessor<T> {

	private static final Log logger = LogFactory.getLog(ClazzAttributeAccessor.class);
	
	private Class<T> clazz;
	
	protected ClazzAttributeAccessor(Class<T> clazz) {
		this.clazz = clazz;
	}

	public <V> Function<T, V> forReading(final String attributeName, 
			final Function<Object, V> caster) {
		
		return instance -> {
				Field fld =	getField(clazz, attributeName);
				
				if (null == fld) {
					logger.error(String.format("field type is not found: %s.%s", 
							clazz.getCanonicalName(), attributeName));
					return null;
				}
				
				boolean accessible = fld.isAccessible();
				fld.setAccessible(true);
				
				Object value;
				try {
					value = fld.get(instance);
				} catch (Exception e) {
					value = null;
				} finally {
					fld.setAccessible(accessible);
				}
				
				return Optional.ofNullable(value)
						.map(caster)
						.orElse(null);
			
		};
		
	}
	
	public <V> BiConsumer<T, V> forWriting(final String attributeName) {
		
		return (T instance, V value) -> {
				Field fld =	getField(clazz, attributeName);
				
				if (null == fld) {
					logger.error(String.format("field is not found: %s.%s", 
							clazz.getCanonicalName(), attributeName));
					return;
				}
				
				boolean accessible = fld.isAccessible();
				fld.setAccessible(true);
				try {
					fld.set(instance, value);
				} catch (Exception e) {
					logger.error(String.format(
							"set value to field fail! (%s.%s = %s)", 
							clazz.getCanonicalName(), attributeName, 
							value.toString()));
				} finally {
					fld.setAccessible(accessible);
				}			
		};
		
	}
	
	private Field getField(Class<?> clz, String attributeName) {
		if (null == clz) {
			return null;
		}
		
		return ReflectionUtils.findField(clz, attributeName);
	}

}
