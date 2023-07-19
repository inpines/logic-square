package org.dotspace.oofp.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class AnnotatedFieldSelection<T extends Annotation> {
	private String name;
	private Field field;
	private T annotation;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public T getAnnotation() {
		return annotation;
	}
	public void setAnnotation(T mandatoryField) {
		this.annotation = mandatoryField;
	}
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
}