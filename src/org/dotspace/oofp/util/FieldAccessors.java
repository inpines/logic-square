package org.dotspace.oofp.util;

import java.lang.reflect.Field;

public class FieldAccessors {
		
	private MemberAccessors memberAccessors = null;
	
	public static FieldAccessors forInstance(Object instance) {
		return new FieldAccessors(instance);
	}
	
	public static FieldAccessors forInstance(Object instance, String path) {
		return new FieldAccessors(instance, path);
		/*
		FieldAccessors fas = FieldAccessors.forInstance(instance);

		if (StringUtils.isBlank(path)) {
			return fas;
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
			
			inst = fas.read(field);
			clazz = inst.getClass();

			if (null != index) {
				Class<?> fieldType = field.getType();
				inst = (Collection.class.isAssignableFrom(fieldType)) ?
						((Collection<?>) inst).toArray()[Integer.parseInt(index)]:
							((Map.class.isAssignableFrom(fieldType)) ?
									inst = ((Map<?, ?>) inst).get(index) : inst);
			}

			fas = FieldAccessors.forInstance(inst);
		}
		return fas;
		*/
	}
	
	private FieldAccessors(Object instance) {
		memberAccessors = new MemberAccessors(instance);
	}

	private FieldAccessors(Object instance, String path) {
		memberAccessors = MemberAccessors.forInstance(instance, path);
	}
	
	public Object read(Field field) {
		return memberAccessors.read(field);
	}
	
	public Object read(String fieldName) {
		return memberAccessors.read(fieldName);
	}
	
	public void write(Field field, Object value) {
		memberAccessors.write(field, value);
	}
	
	public void write(String fieldName, Object value) {
		memberAccessors.write(fieldName, value);
	}
	
}
