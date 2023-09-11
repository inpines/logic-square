package org.dotspace.oofp.support;

import java.util.Map;

public interface ExpressionEvaluation {
	
	public <T> T getValue(Class<T> resultClazz, Map<String, Object> variables, Object root);
	
	public <T> T getValue(Class<T> resultClazz, Object root);

	public <T> T getValueWithVariables(Map<String, Object> variables, Object root);
	
	public <T> T getValue(Object root);
	
	public <T> T getValue();

	public <T> void setValue(Map<String, Object> variables, T root, Object value);
	
	public <T> void setValue(T result, Object value);

}
