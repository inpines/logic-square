package org.dotspace.oofp.support.orm.specification.expression;

public class PathValueLt<T, V extends Comparable<V>>
        extends PathComparisonValue<T, V> {
    public PathValueLt(V value) {
        super(CriteriaBuilderComparisons::pathValueLt, value);
    }

}
