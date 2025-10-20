package org.dotspace.oofp.utils;

import lombok.Getter;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Setter
@Getter
public class AnnotatedFieldSelection<T extends Annotation> {
	private String name;
	private Field field;
	private T annotation;
}