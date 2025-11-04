package org.dotspace.oofp.support.orm.specification.expression;

public class PathValueLe<T, V extends Comparable<V>>
        extends PathComparisonValue<T, V> {

    public PathValueLe(V value) {
        super(CriteriaBuilderComparisons::pathValueLe, value);
    }

}
