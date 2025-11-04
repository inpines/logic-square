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
class AnyTest {

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
    private Predicate orPredicate;

    @Test
    @DisplayName("Constructor should create Any with given predicate expressions")
    void constructor_WithPredicateExpressions_ShouldCreateAny() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);

        // When
        Any<TestEntity> any = new Any<>(expressions);

        // Then
        assertNotNull(any);
    }

    @Test
    @DisplayName("Constructor should handle null predicate expressions")
    void constructor_WithNullExpressions_ShouldCreateAny() {
        // Given & When
        Any<TestEntity> any = new Any<>(null);

        // Then
        assertNotNull(any);
    }

    @Test
    @DisplayName("Constructor should handle empty predicate expressions list")
    void constructor_WithEmptyExpressions_ShouldCreateAny() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Collections.emptyList();

        // When
        Any<TestEntity> any = new Any<>(expressions);

        // Then
        assertNotNull(any);
    }

    @Test
    @DisplayName("getPredicate with multiple expressions should return OR predicate")
    void getPredicate_WithMultipleExpressions_ShouldReturnOrPredicate() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with single expression should return OR predicate")
    void getPredicate_WithSingleExpression_ShouldReturnOrPredicate() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with null expressions should return empty optional")
    void getPredicate_WithNullExpressions_ShouldReturnEmpty() {
        // Given
        Any<TestEntity> any = new Any<>(null);
        String fieldName = "testField";

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verifyNoInteractions(mockExpression1, mockExpression2, criteriaBuilder);
    }

    @Test
    @DisplayName("getPredicate with empty expressions should return empty optional")
    void getPredicate_WithEmptyExpressions_ShouldReturnEmpty() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Collections.emptyList();
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verifyNoInteractions(mockExpression1, mockExpression2, criteriaBuilder);
    }

    @Test
    @DisplayName("getPredicate with expressions returning empty predicates should return empty optional")
    void getPredicate_WithExpressionsReturningEmpty_ShouldReturnEmpty() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder, never()).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with mixed empty and non-empty predicates should return OR of non-empty")
    void getPredicate_WithMixedEmptyAndNonEmpty_ShouldReturnOrOfNonEmpty() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2, mockExpression3);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());
        when(mockExpression3.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate3));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression3).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with three expressions should return OR predicate")
    void getPredicate_WithThreeExpressions_ShouldReturnOrPredicate() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2, mockExpression3);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(mockExpression3.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate3));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression3).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @ParameterizedTest
    @DisplayName("getPredicate should handle different field names")
    @MethodSource("provideFieldNames")
    void getPredicate_WithDifferentFieldNames_ShouldPassCorrectFieldName(String fieldName, String testDescription) {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1);
        Any<TestEntity> any = new Any<>(expressions);

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent(), testDescription);
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
    }

    private static Stream<Arguments> provideFieldNames() {
        return Stream.of(
                Arguments.of("normalField", "Normal field name test"),
                Arguments.of("", "Empty field name test"),
                Arguments.of("field.nested", "Nested field name test"),
                Arguments.of("field_with_underscores", "Field with underscores test"),
                Arguments.of("camelCaseField", "Camel case field name test"),
                Arguments.of("123numericField", "Field name with numbers test")
        );
    }

    @Test
    @DisplayName("getPredicate should be reusable for multiple calls")
    void getPredicate_ShouldBeReusableForMultipleCalls() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName1 = "field1";
        String fieldName2 = "field2";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName1)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName1)).thenReturn(Optional.of(predicate2));
        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName2)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName2)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result1 = any.getPredicate(criteriaBuilder, root, fieldName1);
        Optional<Predicate> result2 = any.getPredicate(criteriaBuilder, root, fieldName2);

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(orPredicate, result1.get());
        assertEquals(orPredicate, result2.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName1);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName1);
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName2);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName2);
        verify(criteriaBuilder, times(2)).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should handle modifiable list")
    void getPredicate_WithModifiableList_ShouldWork() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = new ArrayList<>();
        expressions.add(mockExpression1);
        expressions.add(mockExpression2);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should handle list with null elements gracefully")
    void getPredicate_WithNullElementsInList_ShouldHandleGracefully() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, null, mockExpression2);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        // When & Then
        assertThrows(NullPointerException.class, () -> any.getPredicate(criteriaBuilder, root, fieldName));
    }

    @Test
    @DisplayName("getPredicate should pass correct parameters to child expressions")
    void getPredicate_ShouldPassCorrectParametersToChildExpressions() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "specificField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        any.getPredicate(criteriaBuilder, root, fieldName);

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

        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should handle immutable list")
    void getPredicate_WithImmutableList_ShouldWork() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1, mockExpression2);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should preserve order of predicates")
    void getPredicate_ShouldPreserveOrderOfPredicates() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(mockExpression1, mockExpression2, mockExpression3);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(mockExpression3.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate3));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression3).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate with very large list should be efficient")
    void getPredicate_WithVeryLargeList_ShouldBeEfficient() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            CriteriaPredicateExpression<TestEntity> mockExpr = mock(CriteriaPredicateExpression.class);
            Predicate mockPred = mock(Predicate.class);
            expressions.add(mockExpr);
            when(mockExpr.getPredicate(criteriaBuilder, root, "testField")).thenReturn(Optional.of(mockPred));
        }

        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        long startTime = System.currentTimeMillis();
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);
        long endTime = System.currentTimeMillis();

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(criteriaBuilder).or(any(Predicate[].class));
        assertTrue(endTime - startTime < 1000, "Operation should complete quickly even with large lists");
    }

    @Test
    @DisplayName("getPredicate should handle single non-empty predicate among many empty ones")
    void getPredicate_WithSingleNonEmptyAmongManyEmpty_ShouldReturnOr() {
        // Given
        List<CriteriaPredicateExpression<TestEntity>> expressions = Arrays.asList(
                mockExpression1, mockExpression2, mockExpression3
        );
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());
        when(mockExpression2.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate2));
        when(mockExpression3.getPredicate(criteriaBuilder, root, fieldName)).thenReturn(Optional.empty());
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression2).getPredicate(criteriaBuilder, root, fieldName);
        verify(mockExpression3).getPredicate(criteriaBuilder, root, fieldName);
        verify(criteriaBuilder).or(any(Predicate[].class));
    }

    @Test
    @DisplayName("getPredicate should work with different CriteriaBuilder instances")
    void getPredicate_WithDifferentCriteriaBuilders_ShouldWork() {
        // Given
        CriteriaBuilder otherCriteriaBuilder = mock(CriteriaBuilder.class);
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(otherCriteriaBuilder, root, fieldName)).thenReturn(Optional.of(predicate1));
        when(otherCriteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(otherCriteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(mockExpression1).getPredicate(otherCriteriaBuilder, root, fieldName);
        verify(otherCriteriaBuilder).or(any(Predicate[].class));
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    @DisplayName("getPredicate should work with different Root instances")
    void getPredicate_WithDifferentRoots_ShouldWork() {
        // Given
        Root<TestEntity> otherRoot = mock(Root.class);
        List<CriteriaPredicateExpression<TestEntity>> expressions = List.of(mockExpression1);
        Any<TestEntity> any = new Any<>(expressions);
        String fieldName = "testField";

        when(mockExpression1.getPredicate(criteriaBuilder, otherRoot, fieldName)).thenReturn(Optional.of(predicate1));
        when(criteriaBuilder.or(any(Predicate[].class))).thenReturn(orPredicate);

        // When
        Optional<Predicate> result = any.getPredicate(criteriaBuilder, otherRoot, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(orPredicate, result.get());
        verify(mockExpression1).getPredicate(criteriaBuilder, otherRoot, fieldName);
        verify(criteriaBuilder).or(any(Predicate[].class));
        verifyNoInteractions(root);
    }

    // Test entity class for generic type parameter
    private static class TestEntity {
    }
}
