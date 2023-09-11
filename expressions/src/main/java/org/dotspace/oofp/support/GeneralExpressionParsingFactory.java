package org.dotspace.oofp.support;

public interface GeneralExpressionParsingFactory {

	public <T> GeneralExpressionParsing<T> create(T contextRoot);
	
}
