package org.dotspace.oofp.support;

import java.util.Map;

public interface SpelExpressionParsing<T> {

	public <V> SpelExpressionParsing<T> setVariable(String name, V value);
	
	public SpelExpressionParsing<T> setVariables(Map<String, Object> variables);
	
	public <V> V getValue(String expression, Class<V> valueClazz);
	
}
