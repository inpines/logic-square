package org.dotspace.oofp.support.orm.specification.expression;

import org.dotspace.oofp.support.orm.specification.CriteriaPredicateExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllTest {

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Root<TestEntity> root;

    @Mock
    private CriteriaPredicateExpression<TestEntity> mockExpression1;

    @Mock
    private CriteriaPredicateExpression<TestEntity> mockExpression2;

    @Mock
    private CriteriaPredicateExpression<TestEntity> mockExpression3;

    @Mock
    private Predicate predicate1;

    @Mock
    private Predicate predicate2;

    @Mock
    private Predicate predicate3;

    @Mock
    private Predicate andPredicate;

    @Test
    @DisplayName("Constructor should create All with given predicate expressions")
    void constructor_WithPredicateExpressions_ShouldCreateAll() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);

        // When
        All<TestEntity> all = new All<>(expressions);

        // Then
        assertNotNull(all);
    }

    @Test
    @DisplayName("Constructor should handle null predicate expressions")
    void constructor_WithNullExpressions_ShouldCreateAll() {
        // Given & When
        All<TestEntity> all = new All<>(null);

        // Then
        assertNotNull(all);
    }

    @Test
    @DisplayName("Constructor should handle empty predicate expressions list")
    void constructor_WithEmptyExpressions_ShouldCreateAll() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Collections.emptyList();

        // When
        All<TestEntity> all = new All<>(expressions);

        // Then
        assertNotNull(all);
    }

    @Test
    @DisplayName("getPredicate with multiple expressions should return AND predicate")
    void getPredicate_WithMultipleExpressions_ShouldReturnAndPredicate() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(andPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with single expression should return AND predicate")
    void getPredicate_WithSingleExpression_ShouldReturnAndPredicate() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(andPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with null expressions should return empty optional")
    void getPredicate_WithNullExpressions_ShouldReturnEmpty() {
        // Given
        All<TestEntity> all = new All<>(null);
        String fieldName = "testField";

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verifyNoInteractions(mockExpression1, mockExpression2, criteriaBuilder);
    }

    @Test
    @DisplayName("getPredicate with empty expressions should return empty optional")
    void getPredicate_WithEmptyExpressions_ShouldReturnEmpty() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Collections.emptyList();
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verifyNoInteractions(mockExpression1, mockExpression2, criteriaBuilder);
    }

    @Test
    @DisplayName("getPredicate with expressions returning empty predicates should return empty optional")
    void getPredicate_WithExpressionsReturningEmpty_ShouldReturnEmpty() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder, never()).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with mixed empty and non-empty predicates should return AND of non-empty")
    void getPredicate_WithMixedEmptyAndNonEmpty_ShouldReturnAndOfNonEmpty() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2, mockExpression3);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());
        when(mockExpression3.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate3));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(andPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression3).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with three expressions should return AND predicate")
    void getPredicate_WithThreeExpressions_ShouldReturnAndPredicate() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2, mockExpression3);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(mockExpression3.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate3));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(andPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression3).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @ParameterizedTest
    @DisplayName("getPredicate should handle different field names")
    @MethodSource("provideFieldNames")
    void getPredicate_WithDifferentFieldNames_ShouldPassCorrectFieldName(String fieldName, String testDescription) {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1);
        All<TestEntity> all = new All<>(expressions);

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent(), testDescription);
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
    }

    private static Stream<Arguments> provideFieldNames() {
        return Stream.of(
                Arguments.of("normalField", "Normal field name test"),
                Arguments.of("", "Empty field name test"),
                Arguments.of("field.nested", "Nested field name test"),
                Arguments.of("field_with_underscores", "Field with underscores test")
        );
    }

    @Test
    @DisplayName("getPredicate should be reusable for multiple calls")
    void getPredicate_ShouldBeReusableForMultipleCalls() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        All<TestEntity> all = new All<>(expressions);
        String fieldName1 = "field1";
        String fieldName2 = "field2";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName1)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName1)).thenReturn(Optional.of(predicate2));
        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName2)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName2)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result1 = all.getPredicate(criteriaBuilder, root, fieldName1);
        Optional<Predicate> result2 = all.getPredicate(criteriaBuilder, root, fieldName2);

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(andPredicate, result1.get());
        assertEquals(andPredicate, result2.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName1);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName1);
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName2);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName2);
        verify(criteriaBuilder, times(2)).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should handle modifiable list")
    void getPredicate_WithModifiableList_ShouldWork() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = new ArrayList<>();
        expressions.add(mockExpression1);
        expressions.add(mockExpression2);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(andPredicate, result.get());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should handle list with null elements gracefully")
    void getPredicate_WithNullElementsInList_ShouldHandleGracefully() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, null, mockExpression2);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        // When & Then
        assertThrows(NullPointerException.class, () -> all.getPredicate(criteriaBuilder, root, fieldName));
    }

    @Test
    @DisplayName("getPredicate should pass correct parameters to child expressions")
    void getPredicate_ShouldPassCorrectParametersToChildExpressions() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "specificField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
    }

    @Test
    @DisplayName("getPredicate with large number of expressions should work")
    void getPredicate_WithLargeNumberOfExpressions_ShouldWork() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            CriteriaPredicateExpression<TestEntity> mockExpr = mock(CriteriaPredicateExpression.class);
            Predicate mockPred = mock(Predicate.class);
            expressions.add(mockExpr);
            when(mockExpr.getPredicate(criteriaBuilder, root, "testField")).thenReturn(Optional.of(mockPred));
        }

        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(andPredicate, result.get());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should handle immutable list")
    void getPredicate_WithImmutableList_ShouldWork() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1, mockExpression2);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(andPredicate, result.get());
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should preserve order of predicates")
    void getPredicate_ShouldPreserveOrderOfPredicates() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2, mockExpression3);
        All<TestEntity> all = new All<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(mockExpression3.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate3));
        when(criteriaBuilder.and(any(Predicate[].class))).thenReturn(andPredicate);

        // When
        Optional<Predicate> result = all.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression3).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).and(any(Predicate[].class));
    }

    // Test entity class for generic type parameter
    private static class TestEntity {
    }
}
