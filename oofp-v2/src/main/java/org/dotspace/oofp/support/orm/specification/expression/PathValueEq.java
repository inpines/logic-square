package org.dotspace.oofp.support.orm.specification.expression;

public class PathValueEq<T, V> extends PathComparisonValue<T, V> {
	
	public PathValueEq(V value) {
		super(CriteriaBuilderComparisons::pathValueEq, value);
	}

}
