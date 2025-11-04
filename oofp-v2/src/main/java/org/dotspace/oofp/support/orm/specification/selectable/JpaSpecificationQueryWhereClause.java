package org.dotspace.oofp.support.orm.specification.selectable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaSpecificationQueryWhereClause<T> {

    private final JpaSpecificationQueryContext<T> jpaSpecificationQueryContext;

    private Specification<T> specification;

    public JpaSpecificationQueryWhereClause<T> where(Specification<T> specification) {
        this.specification = specification;
        return this;
    }

    public <D> JpaSpecificationQueryStatement<T, D> select(SelectionsProjection<T, D> selectionsProjection) {
        return new JpaSpecificationQueryStatement<>(jpaSpecificationQueryContext, specification, selectionsProjection);
    }
}
