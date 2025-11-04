package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

@Component
public class JpaSpecificationQuerySupport {

    @PersistenceContext
    private EntityManager entityManager;

    public <T> JpaSpecificationQueryWhereClause<T> from(Root<T> entityRoot, JpaJoinRegistry<T> joinRegistry) {
        return new JpaSpecificationQueryWhereClause<>(JpaSpecificationQueryContext
                .<T>builder()
                .withEntityManager(entityManager)
                .withEntityRoot(entityRoot)
                .withJoinRegistry(joinRegistry)
                .build()
        );
    }

    public <T> JpaSpecificationQueryWhereClause<T> from(Root<T> entityRoot) {
        return from(entityRoot, new JpaJoinRegistry<>(entityRoot));
    }

}
