package org.dotspace.oofp.support.orm.specification.expression;

public class PathValueGe<T, V extends Comparable<V>>
		extends PathComparisonValue<T, V> {

	public PathValueGe(V value) {
		super(CriteriaBuilderComparisons::pathValueGe, value);
	}

}
