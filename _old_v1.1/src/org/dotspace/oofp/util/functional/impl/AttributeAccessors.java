package org.dotspace.oofp.util.functional.impl;

public class AttributeAccessors {

	private AttributeAccessors() {
		super();
	}
	
	public static <T> ClazzAttributeAccessor<T> forClazz(Class<T> clazz) {
		return new ClazzAttributeAccessor<>(clazz);
	}
	
}
