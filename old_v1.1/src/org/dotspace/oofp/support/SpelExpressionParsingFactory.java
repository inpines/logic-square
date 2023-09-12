package org.dotspace.oofp.support;

public interface SpelExpressionParsingFactory {

	public <T> SpelExpressionParsing<T> create(T contextRoot);
	
}
