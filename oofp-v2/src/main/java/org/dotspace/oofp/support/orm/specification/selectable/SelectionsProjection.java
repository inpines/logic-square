package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;

import java.util.List;

public interface SelectionsProjection<T, D> {

    /**
     * 提供 Criteria API 所需的 Selection 列表。
     */
    List<Selection<Object>> selections(Root<T> root, CriteriaBuilder cb, JpaJoinRegistry<T> joinRegistry);

    /**
     * 將查詢結果 Tuple 映射為 DTO。
     */
    D convert(Tuple tuple);

}
