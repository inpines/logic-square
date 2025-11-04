package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JpaJoinRegistry<T> {

    private final Root<T> root;
    private final Map<String, Join<Object, Object>> joins = new HashMap<>();

    public JpaJoinRegistry(Root<T> root) {
        this.root = root;
    }

    public <J> Join<T, J> join(String attributeName) {
        return join(attributeName, JoinType.INNER);
    }

    public <J> Join<T, J> join(String attributeName, JoinType joinType) {
        @SuppressWarnings("unchecked")
        Join<T, J> join = (Join<T, J>) joins.computeIfAbsent(attributeName,
                key -> root.join(key, joinType));
        return join;
    }

    public <J> J apply(String attributeName, Function<Join<?, ?>, J> resolver) {
        return resolver.apply(joins.get(attributeName));
    }

    public boolean hasJoin(String attributeName) {
        return joins.containsKey(attributeName);
    }

    public Map<String, Join<Object, Object>> getAllJoins() {
        return joins;
    }
}
