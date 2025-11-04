package org.dotspace.oofp.support.orm.specification.selectable.builder;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.dotspace.oofp.support.orm.specification.selectable.JpaJoinRegistry;
import org.dotspace.oofp.support.orm.specification.selectable.SelectionsProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelectionsBuilderTest {

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<Integer> integerPath;

    @Mock
    private Path<Long> longPath;

    @Mock
    private Path<Boolean> booleanPath;

    @Mock
    private Tuple tuple;

    @Mock
    private Root<TestEntity> root;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private JpaJoinRegistry<TestEntity> joinRegistry;

    private SelectionsBuilder<TestDto> selectionsBuilder;

    @BeforeEach
    void setUp() {
        selectionsBuilder = new SelectionsBuilder<>();
    }

    @Test
    @DisplayName("select should add path to selections and return builder")
    void select_WithValidPath_ShouldAddPathAndReturnBuilder() {
        // Given
        String alias = "testAlias";

        // When
        SelectionsBuilder<TestDto> result = selectionsBuilder.select(stringPath, alias, String.class);

        // Then
        assertSame(selectionsBuilder, result);
        verify(stringPath).alias(alias);
    }

    @Test
    @DisplayName("select should handle null path")
    void select_WithNullPath_ShouldThrowException() {
        // Given
        String alias = "testAlias";

        // When & Then
        assertThrows(NullPointerException.class, () ->
                selectionsBuilder.select(null, alias, String.class)
        );
    }

    @Test
    @DisplayName("select should handle null alias")
    void select_WithNullAlias_ShouldWork() {
        // Given & When
        SelectionsBuilder<TestDto> result = selectionsBuilder.select(stringPath, null, String.class);

        // Then
        assertSame(selectionsBuilder, result);
        verify(stringPath).alias(null);
    }

    @Test
    @DisplayName("select should handle null type")
    void select_WithNullType_ShouldWork() {
        // Given
        String alias = "testAlias";

        // When
        SelectionsBuilder<TestDto> result = selectionsBuilder.select(stringPath, alias, null);

        // Then
        assertSame(selectionsBuilder, result);
        verify(stringPath).alias(alias);
    }

    @Test
    @DisplayName("select should handle empty string alias")
    void select_WithEmptyStringAlias_ShouldWork() {
        // Given
        String alias = "";

        // When
        SelectionsBuilder<TestDto> result = selectionsBuilder.select(stringPath, alias, String.class);

        // Then
        assertSame(selectionsBuilder, result);
        verify(stringPath).alias(alias);
    }

    @Test
    @DisplayName("select should allow multiple selections")
    void select_WithMultipleSelections_ShouldWork() {
        // Given
        String alias1 = "alias1";
        String alias2 = "alias2";
        String alias3 = "alias3";

        // When
        SelectionsBuilder<TestDto> result = selectionsBuilder
                .select(stringPath, alias1, String.class)
                .select(integerPath, alias2, Integer.class)
                .select(longPath, alias3, Long.class);

        // Then
        assertSame(selectionsBuilder, result);
        verify(stringPath).alias(alias1);
        verify(integerPath).alias(alias2);
        verify(longPath).alias(alias3);
    }

    @Test
    @DisplayName("select should handle same alias multiple times")
    void select_WithSameAlias_ShouldWork() {
        // Given
        String alias = "sameAlias";

        // When
        SelectionsBuilder<TestDto> result = selectionsBuilder
                .select(stringPath, alias, String.class)
                .select(integerPath, alias, Integer.class);

        // Then
        assertSame(selectionsBuilder, result);
        verify(stringPath).alias(alias);
        verify(integerPath).alias(alias);
    }

    @Test
    @DisplayName("buildSelections should return empty list initially")
    void buildSelections_WithoutSelections_ShouldReturnEmptyList() {
        // Given & When
        List<Selection<Object>> result = selectionsBuilder.buildSelections();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("buildSelections should return selections after adding")
    void buildSelections_WithSelections_ShouldReturnSelections() {
        // Given
        selectionsBuilder.select(stringPath, "alias1", String.class);
        selectionsBuilder.select(integerPath, "alias2", Integer.class);

        // When
        List<Selection<Object>> result = selectionsBuilder.buildSelections();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("buildSelections should return same list instance")
    void buildSelections_ShouldReturnSameListInstance() {
        // Given
        selectionsBuilder.select(stringPath, "alias1", String.class);

        // When
        List<Selection<Object>> result1 = selectionsBuilder.buildSelections();
        selectionsBuilder.select(integerPath, "alias2", Integer.class);
        List<Selection<Object>> result2 = selectionsBuilder.buildSelections();

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2); // Same list instance
        assertEquals(2, result1.size()); // Both lists have same size
        assertEquals(2, result2.size());
    }

    @Test
    @DisplayName("map should apply mapper function with extracted values")
    void map_WithValidTupleAndMapper_ShouldReturnMappedResult() {
        // Given
        selectionsBuilder.select(stringPath, "alias1", String.class);
        selectionsBuilder.select(integerPath, "alias2", Integer.class);

        when(tuple.get("alias1", String.class)).thenReturn("testValue");
        when(tuple.get("alias2", Integer.class)).thenReturn(42);

        Function<List<Object>, TestDto> mapper = values -> new TestDto((String) values.get(0), (Integer) values.get(1));

        // When
        TestDto result = selectionsBuilder.map(tuple, mapper);

        // Then
        assertNotNull(result);
        assertEquals("testValue", result.name());
        assertEquals(42, result.age());
        verify(tuple).get("alias1", String.class);
        verify(tuple).get("alias2", Integer.class);
    }

    @Test
    @DisplayName("map should handle empty selections")
    void map_WithEmptySelections_ShouldWork() {
        // Given
        Function<List<Object>, String> mapper = values -> "empty";

        // When
        String result = selectionsBuilder.map(tuple, mapper);

        // Then
        assertEquals("empty", result);
        verifyNoInteractions(tuple);
    }

    @Test
    @DisplayName("map should handle null tuple")
    void map_WithNullTuple_ShouldThrowException() {
        // Given
        selectionsBuilder.select(stringPath, "alias1", String.class);
        Function<List<Object>, String> mapper = values -> "result";

        // When & Then
        assertThrows(NullPointerException.class, () ->
                selectionsBuilder.map(null, mapper));
    }

    @Test
    @DisplayName("map should handle null mapper")
    void map_WithNullMapper_ShouldThrowException() {
        // Given
        selectionsBuilder.select(stringPath, "alias1", String.class);
        when(tuple.get("alias1", String.class)).thenReturn("testValue");

        // When & Then
        assertThrows(NullPointerException.class, () ->
                selectionsBuilder.map(tuple, null));
    }

    @Test
    @DisplayName("map should handle tuple extraction returning null")
    void map_WithTupleReturningNull_ShouldWork() {
        // Given
        selectionsBuilder.select(stringPath, "alias1", String.class);
        when(tuple.get("alias1", String.class)).thenReturn(null);
        Function<List<Object>, String> mapper = values -> values.get(0) == null ? "null" : values.get(0).toString();

        // When
        String result = selectionsBuilder.map(tuple, mapper);

        // Then
        assertEquals("null", result);
    }

    @Test
    @DisplayName("map should handle mapper throwing exception")
    void map_WithMapperThrowingException_ShouldPropagateException() {
        // Given
        selectionsBuilder.select(stringPath, "alias1", String.class);
        when(tuple.get("alias1", String.class)).thenReturn("testValue");
        Function<List<Object>, String> mapper = values -> {
            throw new RuntimeException("Mapper error");
        };

        // When & Then
        assertThrows(RuntimeException.class, () ->
                selectionsBuilder.map(tuple, mapper));
    }

    @Test
    @DisplayName("toProjection should create working SelectionsProjection")
    void toProjection_ShouldCreateWorkingSelectionsProjection() {
        // Given
        Function<SelectionsBuilder<TestDto>, List<Selection<Object>>> selectionFunction = builder -> {
            builder.select(stringPath, "name", String.class);
            builder.select(integerPath, "age", Integer.class);
            return builder.buildSelections();
        };
        Function<List<Object>, TestDto> mappingFunction = values ->
                new TestDto((String) values.get(0), (Integer) values.get(1));

        // When
        SelectionsProjection<TestEntity, TestDto> projection =
                SelectionsBuilder.toProjection(selectionFunction, mappingFunction);

        // Then
        assertNotNull(projection);
        assertInstanceOf(SelectionsProjection.class, projection);
    }

    @Test
    @DisplayName("toProjection selections method should work")
    void toProjection_SelectionsMethod_ShouldWork() {
        // Given
        Function<SelectionsBuilder<TestDto>, List<Selection<Object>>> selectionFunction = builder -> {
            builder.select(stringPath, "name", String.class);
            return builder.buildSelections();
        };
        Function<List<Object>, TestDto> mappingFunction = values -> new TestDto("test", 0);
        SelectionsProjection<TestEntity, TestDto> projection =
                SelectionsBuilder.toProjection(selectionFunction, mappingFunction);

        // When
        List<Selection<Object>> result = projection.selections(root, criteriaBuilder, joinRegistry);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("toProjection convert method should work")
    void toProjection_ConvertMethod_ShouldWork() {
        // Given
        Function<SelectionsBuilder<TestDto>, List<Selection<Object>>> selectionFunction = builder -> Collections.emptyList();
        Function<List<Object>, TestDto> mappingFunction = values -> new TestDto("converted", 42);
        SelectionsProjection<TestEntity, TestDto> projection =
                SelectionsBuilder.toProjection(selectionFunction, mappingFunction);

        // When
        TestDto result = projection.convert(tuple);

        // Then
        assertNotNull(result);
        assertEquals("converted", result.name());
        assertEquals(42, result.age());
    }

    @Test
    @DisplayName("toProjection should handle null selection function when selections is called")
    void toProjection_WithNullSelectionFunction_ShouldThrowExceptionWhenCalled() {
        // Given
        Function<List<Object>, TestDto> mappingFunction = values -> new TestDto("test", 0);
        SelectionsProjection<TestEntity, TestDto> projection =
                SelectionsBuilder.toProjection(null, mappingFunction);

        // When & Then
        assertThrows(NullPointerException.class, () ->
                projection.selections(root, criteriaBuilder, joinRegistry));
    }

    @Test
    @DisplayName("toProjection should handle null mapping function when convert is called")
    void toProjection_WithNullMappingFunction_ShouldThrowExceptionWhenCalled() {
        // Given
        Function<SelectionsBuilder<TestDto>, List<Selection<Object>>> selectionFunction = builder -> Collections.emptyList();
        SelectionsProjection<TestEntity, TestDto> projection =
                SelectionsBuilder.toProjection(selectionFunction, null);

        // When & Then
        assertThrows(NullPointerException.class, () ->
                projection.convert(tuple));
    }

    @Test
    @DisplayName("forRecord should create working SelectionsProjection")
    void forRecord_ShouldCreateWorkingSelectionsProjection() {
        // Given
        Function<SelectionsBuilder<TestRecord>, List<Selection<Object>>> selectionFunction = builder -> {
            builder.select(stringPath, "name", String.class);
            builder.select(integerPath, "age", Integer.class);
            return builder.buildSelections();
        };

        // When
        SelectionsProjection<TestEntity, TestRecord> projection =
                SelectionsBuilder.forRecord(TestRecord.class, selectionFunction,
                        (ex, msg) -> new RuntimeException(msg, ex));

        // Then
        assertNotNull(projection);
        assertInstanceOf(SelectionsProjection.class, projection);
    }

    @Test
    @DisplayName("forRecord selections method should work")
    void forRecord_SelectionsMethod_ShouldWork() {
        // Given
        Function<SelectionsBuilder<TestRecord>, List<Selection<Object>>> selectionFunction = builder -> {
            builder.select(stringPath, "name", String.class);
            return builder.buildSelections();
        };
        SelectionsProjection<TestEntity, TestRecord> projection =
                SelectionsBuilder.forRecord(TestRecord.class, selectionFunction,
                        (ex, msg) -> new RuntimeException(msg, ex));

        // When
        List<Selection<Object>> result = projection.selections(root, criteriaBuilder, joinRegistry);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("forRecord convert method should work with valid record")
    void forRecord_ConvertMethod_ShouldWork() {
        // Given
        Function<SelectionsBuilder<PublicTestRecord>, List<Selection<Object>>> selectionFunction = builder -> Collections.emptyList();
        SelectionsProjection<TestEntity, PublicTestRecord> projection =
                SelectionsBuilder.forRecord(PublicTestRecord.class, selectionFunction,
                        (ex, msg) -> new RuntimeException(msg, ex));

        when(tuple.get("name", String.class)).thenReturn("testName");
        when(tuple.get("age", Integer.class)).thenReturn(25);

        // When
        PublicTestRecord result = projection.convert(tuple);

        // Then
        assertNotNull(result);
        assertEquals("testName", result.name());
        assertEquals(25, result.age());
    }

    @Test
    @DisplayName("forRecord should handle null record class when convert is called")
    void forRecord_WithNullRecordClass_ShouldThrowExceptionWhenCalled() {
        // Given
        Function<SelectionsBuilder<TestRecord>, List<Selection<Object>>> selectionFunction = builder -> Collections.emptyList();
        SelectionsProjection<TestEntity, TestRecord> projection =
                SelectionsBuilder.forRecord(null, selectionFunction,
                        (ex, msg) -> new RuntimeException(msg, ex));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                projection.convert(tuple));
    }

    @Test
    @DisplayName("forRecord should handle null selection function when selections is called")
    void forRecord_WithNullSelectionFunction_ShouldThrowExceptionWhenCalled() {
        // Given
        SelectionsProjection<TestEntity, TestRecord> projection =
                SelectionsBuilder.forRecord(TestRecord.class, null,
                        (ex, msg) -> new RuntimeException(msg, ex));

        // When & Then
        assertThrows(NullPointerException.class, () ->
                projection.selections(root, criteriaBuilder, joinRegistry));
    }

    @Test
    @DisplayName("forRecord convert should handle tuple get throwing exception")
    void forRecord_ConvertWithTupleException_ShouldPropagateException() {
        // Given
        Function<SelectionsBuilder<TestRecord>, List<Selection<Object>>> selectionFunction = builder -> Collections.emptyList();
        SelectionsProjection<TestEntity, TestRecord> projection =
                SelectionsBuilder.forRecord(TestRecord.class, selectionFunction,
                        (ex, msg) -> new RuntimeException(msg, ex));

        when(tuple.get("name", String.class)).thenThrow(new RuntimeException("Tuple error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                projection.convert(tuple));
        assertTrue(exception.getMessage().contains("Failed to convert tuple to record"));
    }

    @Test
    @DisplayName("forRecord convert should handle constructor access issues")
    void forRecord_ConvertWithConstructorIssues_ShouldThrowRuntimeException() {
        // Given
        Function<SelectionsBuilder<TestRecordPrivate>, List<Selection<Object>>> selectionFunction = builder -> Collections.emptyList();
        SelectionsProjection<TestEntity, TestRecordPrivate> projection =
                SelectionsBuilder.forRecord(TestRecordPrivate.class, selectionFunction,
                        (ex, msg) -> new RuntimeException(msg, ex));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                projection.convert(tuple));
        assertTrue(exception.getMessage().contains("Failed to convert tuple to record"));
    }

    @Test
    @DisplayName("map should handle large number of selections")
    void map_WithLargeNumberOfSelections_ShouldWork() {
        // Given
        for (int i = 0; i < 50; i++) {
            Path<String> path = mock(Path.class);
            String alias = "alias" + i;
            selectionsBuilder.select(path, alias, String.class);
            when(tuple.get(alias, String.class)).thenReturn("value" + i);
        }
        Function<List<Object>, Integer> mapper = List::size;

        // When
        Integer result = selectionsBuilder.map(tuple, mapper);

        // Then
        assertEquals(50, result);
    }

    @Test
    @DisplayName("chaining select calls should work")
    void chainingSelectCalls_ShouldWork() {
        // Given & When
        SelectionsBuilder<TestDto> result = selectionsBuilder
                .select(stringPath, "name", String.class)
                .select(integerPath, "age", Integer.class)
                .select(booleanPath, "active", Boolean.class)
                .select(longPath, "id", Long.class);

        // Then
        assertSame(selectionsBuilder, result);
        List<Selection<Object>> selections = selectionsBuilder.buildSelections();
        assertEquals(4, selections.size());
    }

    @Test
    @DisplayName("buildSelections should return modifiable list")
    void buildSelections_ShouldReturnModifiableList() {
        // Given
        selectionsBuilder.select(stringPath, "alias", String.class);

        // When
        List<Selection<Object>> result = selectionsBuilder.buildSelections();

        // Then
        assertDoesNotThrow(() -> result.add(mock(Selection.class)));
    }

    // Test classes and records
    private record TestDto(String name, Integer age) {}

    private record TestRecord(String name, Integer age) {}

    // Private record to test constructor access issues
    private record TestRecordPrivate(String name, Integer age) {
    }

    private static class TestEntity {
    }

    public record PublicTestRecord(String name, Integer age) {}
}
