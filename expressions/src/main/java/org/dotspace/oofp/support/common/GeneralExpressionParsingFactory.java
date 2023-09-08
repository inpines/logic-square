package org.dotspace.oofp.support.common;

public interface GeneralExpressionParsingFactory {

	public <T> GeneralExpressionParsing<T> create(T contextRoot);
	
}
