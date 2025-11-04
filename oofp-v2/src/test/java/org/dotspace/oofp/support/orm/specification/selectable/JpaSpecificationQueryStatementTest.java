package org.dotspace.oofp.support.orm.specification.selectable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JpaSpecificationQueryStatementTest {

    @Mock
    private JpaSpecificationQueryContext<TestEntity> queryContext;

    @Mock
    private Specification<TestEntity> specification;

    @Mock
    private SelectionsProjection<TestEntity, TestDto> selectionsProjection;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Tuple> criteriaQuery;

    @Mock
    private Root<TestEntity> root;

    @Mock
    private JpaJoinRegistry<TestEntity> joinRegistry;

    @Mock
    private TypedQuery<Tuple> typedQuery;

    @Mock
    private Tuple tuple1;

    @Mock
    private Tuple tuple2;

    @Mock
    private Selection<Object> selection1;

    @Mock
    private Selection<Object> selection2;

    @Mock
    private Predicate predicate;

    private JpaSpecificationQueryStatement<TestEntity, TestDto> queryStatement;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(queryContext.entityManager()).thenReturn(entityManager);
        when(queryContext.entityRoot()).thenReturn(root);
        when(queryContext.joinRegistry()).thenReturn(joinRegistry);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createTupleQuery()).thenReturn(criteriaQuery);
        when(criteriaBuilder.tuple(any())).thenReturn(mock(CompoundSelection.class));
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);

        queryStatement = new JpaSpecificationQueryStatement<>(queryContext, specification, selectionsProjection);
    }

    @Test
    @DisplayName("evaluate should return empty list when no results")
    void evaluate_WithNoResults_ShouldReturnEmptyList() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(Arrays.asList(selection1, selection2));
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        List<TestDto> result = queryStatement.evaluate();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should return converted results")
    void evaluate_WithResults_ShouldReturnConvertedResults() {
        // Given
        List<Selection<Object>> selections = Arrays.asList(selection1, selection2);
        TestDto dto1 = new TestDto("John", 25);
        TestDto dto2 = new TestDto("Jane", 30);

        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry)).thenReturn(selections);
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(tuple1, tuple2));
        when(selectionsProjection.convert(tuple1)).thenReturn(dto1);
        when(selectionsProjection.convert(tuple2)).thenReturn(dto2);

        // When
        List<TestDto> result = queryStatement.evaluate();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
        verify(entityManager).close();
        verify(selectionsProjection).convert(tuple1);
        verify(selectionsProjection).convert(tuple2);
    }

    @Test
    @DisplayName("evaluate should handle null specification")
    void evaluate_WithNullSpecification_ShouldWork() {
        // Given
        queryStatement = new JpaSpecificationQueryStatement<>(queryContext, null, selectionsProjection);
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(List.of(selection1));
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        List<TestDto> result = queryStatement.evaluate();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(criteriaQuery, never()).where(any(Predicate.class));
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should handle specification returning null predicate")
    void evaluate_WithNullPredicate_ShouldWork() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(List.of(selection1));
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(null);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        List<TestDto> result = queryStatement.evaluate();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(criteriaQuery, never()).where(any(Predicate.class));
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should apply specification predicate when not null")
    void evaluate_WithValidPredicate_ShouldApplyWhere() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(List.of(selection1));
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        List<TestDto> result = queryStatement.evaluate();

        // Then
        assertNotNull(result);
        verify(criteriaQuery).where(predicate);
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should handle empty selections")
    void evaluate_WithEmptySelections_ShouldWork() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(Collections.emptyList());
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        List<TestDto> result = queryStatement.evaluate();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(criteriaBuilder).tuple();  // No arguments when selections are empty
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should close entity manager even when exception occurs")
    void evaluate_WithException_ShouldCloseEntityManager() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenThrow(new RuntimeException("Selection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> queryStatement.evaluate());
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should handle projection conversion exception")
    void evaluate_WithConversionException_ShouldPropagateException() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(List.of(selection1));
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenReturn(List.of(tuple1));
        when(selectionsProjection.convert(tuple1)).thenThrow(new RuntimeException("Conversion error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> queryStatement.evaluate());
        assertEquals("Conversion error", exception.getMessage());
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should handle query execution exception")
    void evaluate_WithQueryException_ShouldPropagateException() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(List.of(selection1));
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenThrow(new RuntimeException("Query error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> queryStatement.evaluate());
        assertEquals("Query error", exception.getMessage());
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should create tuple with correct selections array")
    void evaluate_ShouldCreateTupleWithCorrectSelectionsArray() {
        // Given
        List<Selection<Object>> selections = Arrays.asList(selection1, selection2);
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry)).thenReturn(selections);
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        queryStatement.evaluate();

        // Then
        verify(criteriaBuilder).tuple(selection1, selection2);
        verify(entityManager).close();
    }

    @Test
    @DisplayName("evaluate should handle large result sets")
    void evaluate_WithLargeResultSet_ShouldWork() {
        // Given
        List<Tuple> largeTupleList = Collections.nCopies(1000, tuple1);
        TestDto dto = new TestDto("Test", 1);

        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(List.of(selection1));
        when(specification.toPredicate(root, criteriaQuery, criteriaBuilder)).thenReturn(predicate);
        when(typedQuery.getResultList()).thenReturn(largeTupleList);
        when(selectionsProjection.convert(tuple1)).thenReturn(dto);

        // When
        List<TestDto> result = queryStatement.evaluate();

        // Then
        assertNotNull(result);
        assertEquals(1000, result.size());
        assertTrue(result.stream().allMatch(d -> d.equals(dto)));
        verify(entityManager).close();
    }

    @Test
    @DisplayName("constructor should accept all required parameters")
    void constructor_WithValidParameters_ShouldCreateInstance() {
        // Given & When
        JpaSpecificationQueryStatement<TestEntity, TestDto> statement =
                new JpaSpecificationQueryStatement<>(queryContext, specification, selectionsProjection);

        // Then
        assertNotNull(statement);
    }

    @Test
    @DisplayName("constructor should accept null specification")
    void constructor_WithNullSpecification_ShouldCreateInstance() {
        // Given & When
        JpaSpecificationQueryStatement<TestEntity, TestDto> statement =
                new JpaSpecificationQueryStatement<>(queryContext, null, selectionsProjection);

        // Then
        assertNotNull(statement);
    }

    @Test
    @DisplayName("evaluate should use try-with-resources pattern correctly")
    void evaluate_ShouldUseTryWithResourcesPattern() {
        // Given
        when(selectionsProjection.selections(root, criteriaBuilder, joinRegistry))
                .thenReturn(List.of(selection1));
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // When
        queryStatement.evaluate();

        // Then
        verify(queryContext).entityManager();
        verify(entityManager).close();
    }

    // Test classes
    private record TestDto(String name, Integer age) {}

    private static class TestEntity {}
}
