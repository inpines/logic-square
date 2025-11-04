package org.dotspace.oofp.support.orm.specification.expression;

public class PathValueGt<T, V extends Comparable<V>> extends PathComparisonValue<T, V> {

	public PathValueGt(V value) {
		super(CriteriaBuilderComparisons::pathValueGt, value);
	}

}
