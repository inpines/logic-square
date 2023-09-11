package org.dotspace.oofp.support;

import java.util.Map;

public interface GeneralExpressionParsing<T> {

	public <V> GeneralExpressionParsing<T> setVariable(String name, V value);
	
	public GeneralExpressionParsing<T> setVariables(Map<String, Object> variables);
	
	public <V> V getValue(String expression, Class<V> valueClazz);
	
}
