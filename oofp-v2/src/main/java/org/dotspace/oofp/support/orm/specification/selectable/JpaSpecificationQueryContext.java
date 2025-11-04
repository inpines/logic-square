package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Root;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record JpaSpecificationQueryContext<T> (
    EntityManager entityManager,
    Root<T> entityRoot,
    JpaJoinRegistry<T> joinRegistry
) {
}
