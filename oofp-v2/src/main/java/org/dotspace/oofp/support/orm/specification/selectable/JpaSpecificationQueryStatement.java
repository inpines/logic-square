package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@RequiredArgsConstructor
public class JpaSpecificationQueryStatement<T, D> {

    private final JpaSpecificationQueryContext<T> jpaSpecificationQueryContext;

    private final Specification<T> specification;

    private final SelectionsProjection<T, D> selectionsProjection;

    public List<D> evaluate() {
        try (EntityManager entityManager = jpaSpecificationQueryContext.entityManager()) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Tuple> query = cb.createTupleQuery();

            Root<T> root = jpaSpecificationQueryContext.entityRoot();
            JpaJoinRegistry<T> joinRegistry = jpaSpecificationQueryContext.joinRegistry();
            query.select(cb.tuple(selectionsProjection.selections(root, cb, joinRegistry)
                    .toArray(new Selection[0])));

            if (specification != null) {
                Predicate predicate = specification.toPredicate(root, query, cb);
                if (predicate != null) {
                    query.where(predicate);
                }
            }

            return entityManager
                    .createQuery(query)
                    .getResultList()
                    .stream()
                    .map(selectionsProjection::convert)
                    .toList();
        }

    }

}
