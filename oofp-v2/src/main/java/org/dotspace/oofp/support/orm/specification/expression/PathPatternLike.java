package org.dotspace.oofp.support.orm.specification.expression;

public class PathPatternLike<T> extends PathComparisonValue<T, String> //implements CriteriaPredicateExpression<T> 
{
	
	public PathPatternLike(String pattern) {
		super(CriteriaBuilderComparisons::pathValueLike, pattern);
	}

}
