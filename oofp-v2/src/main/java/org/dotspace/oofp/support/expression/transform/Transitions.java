package org.dotspace.oofp.support.expression.transform;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a collection of transformation mappings.
 * This class provides methods to create a collection of mappings from a map of rules,
 * filter the mappings, and retrieve all source expressions and target fields.
 */
public class Transitions {

    private final List<TransformMapping> mappings;

    private Transitions(List<TransformMapping> mappings) {
        this.mappings = List.copyOf(mappings);
    }

    /**
     * Creates a Transitions instance from a list of TransformMapping.
     *
     * @param mappings a list of TransformMapping objects
     * @return a Transitions instance containing the mappings
     */
    public static Transitions of(List<TransformMapping> mappings) {
        return new Transitions(mappings);
    }

    /**
     * Creates a Transitions instance from a map of mapping rules.
     * Each entry in the map is converted to a TransformMapping.
     *
     * @param mappingRules a map where keys are reader expressions and values are writer expressions
     * @return a Transitions instance containing the mappings
     */
    public static Transitions from(Map<String, String> mappingRules) {
        return new Transitions(
                mappingRules.entrySet().stream()
                        .map(e -> new TransformMapping(
                                e.getKey(), e.getValue()))
                        .toList()
        );
    }

    public Stream<TransformMapping> stream() {
        return mappings.stream();
    }

    public Set<String> allSourceExpressions() {
        return mappings.stream().map(TransformMapping::readerExpr).collect(Collectors.toSet());
    }

    public Set<String> allTargetFields() {
        return mappings.stream().map(TransformMapping::writerExpr).collect(Collectors.toSet());
    }

    public Transitions filter(Predicate<TransformMapping> predicate) {
        return new Transitions(mappings.stream().filter(predicate).toList());
    }

    public Transitions merge(
            Transitions other, TransitionOptions options,
            Function<String, RuntimeException> exceptionSupplier) {
        Map<String, TransformMapping> merged = new LinkedHashMap<>();
        for (TransformMapping m : this.mappings) {
            merged.put(m.writerExpr(), m);
        }
        for (TransformMapping m : other.mappings) {
            boolean hasConflict = merged.containsKey(m.writerExpr());
            if (hasConflict && !options.isOverwriting()) {
                throw exceptionSupplier.apply(String.format("Duplicate readerExpr: %s", m.writerExpr()));
            }
            merged.put(m.writerExpr(), m); // 覆蓋或新增
        }
        return new Transitions(new ArrayList<>(merged.values()));
    }

}
