package org.dotspace.oofp.support.orm.specification.selectable.builder;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.dotspace.oofp.support.orm.specification.selectable.JpaJoinRegistry;
import org.dotspace.oofp.support.orm.specification.selectable.SelectionsProjection;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 用於輔助構建 selections 清單與 tuple 轉換映射的建構器。
 */
public class SelectionsBuilder<D> {

    private final List<Selection<Object>> selections = new ArrayList<>();
    private final List<Function<Tuple, Object>> extractors = new ArrayList<>();

    public <T> SelectionsBuilder<D> select(Path<T> path, String alias, Class<T> type) {
        path.alias(alias);

        @SuppressWarnings("unchecked")
        Selection<Object> pathValue = (Selection<Object>) path;

        selections.add(pathValue);
        extractors.add(tuple -> tuple.get(alias, type));
        return this;
    }

    public List<Selection<Object>> buildSelections() {
        return selections;
    }

    public <R> R map(Tuple tuple, Function<List<Object>, R> mapper) {
        List<Object> values = new ArrayList<>();
        for (Function<Tuple, ?> extractor : extractors) {
            values.add(extractor.apply(tuple));
        }
        return mapper.apply(values);
    }

    /**
     * 快捷生成 SelectionsProjection 實例
     */
    public static <T, D> SelectionsProjection<T, D> toProjection(
            Function<SelectionsBuilder<D>, List<Selection<Object>>> selectionFunction,
            Function<List<Object>, D> mappingFunction) {
        return new SelectionsProjection<>() {
            @Override
            public List<Selection<Object>> selections(
                    jakarta.persistence.criteria.Root<T> root,
                    CriteriaBuilder cb,
                    JpaJoinRegistry<T> joinRegistry
            ) {
                SelectionsBuilder<D> builder = new SelectionsBuilder<>();
                return selectionFunction.apply(builder);
            }

            @Override
            public D convert(Tuple tuple) {
                SelectionsBuilder<D> builder = new SelectionsBuilder<>();
                return builder.map(tuple, mappingFunction);
            }
        };
    }

    /**
     * 支援 Java Record 的自動轉換：根據欄位名稱自動對應 alias 並透過 canonical constructor 建構
     */
    public static <T, D extends Record> SelectionsProjection<T, D> forRecord(
            Class<D> dtoType, Function<SelectionsBuilder<D>, List<Selection<Object>>> selectionFunction,
            BiFunction<Exception, String, RuntimeException> exceptionGenerator) {
        return new SelectionsProjection<>() {
            @Override
            public List<Selection<Object>> selections(
                    Root<T> root, CriteriaBuilder cb, JpaJoinRegistry<T> joinRegistry) {
                SelectionsBuilder<D> builder = new SelectionsBuilder<>();
                return selectionFunction.apply(builder);
            }

            @Override
            public D convert(Tuple tuple) {
                try {
                    RecordComponent[] components = dtoType.getRecordComponents();
                    Object[] args = new Object[components.length];
                    for (int i = 0; i < components.length; i++) {
                        String name = components[i].getName();
                        Class<?> type = components[i].getType();
                        args[i] = tuple.get(name, type);
                    }
                    Constructor<D> constructor = dtoType.getDeclaredConstructor(
                            Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new));
                    return constructor.newInstance(args);
                } catch (Exception e) {
                    throw exceptionGenerator.apply(e, "Failed to convert tuple to record: " + dtoType.getName());
                }
            }
        };
    }

}
