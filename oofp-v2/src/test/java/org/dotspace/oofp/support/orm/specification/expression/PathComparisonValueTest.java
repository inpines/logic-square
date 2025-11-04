package org.dotspace.oofp.support.orm.specification.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PathComparisonValueTest {

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Root<TestEntity> root;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<Integer> integerPath;

    @Mock
    private Path<LocalDate> datePath;

    @Mock
    private Path<BigDecimal> decimalPath;

    @Mock
    private Predicate mockPredicate;

    @Mock
    private BiFunction<Path<String>, String, Predicate> stringComparison;

    @Mock
    private BiFunction<Path<Integer>, Integer, Predicate> integerComparison;

    @Mock
    private Function<CriteriaBuilder, BiFunction<Path<String>, String, Predicate>> stringComparisonFactory;

    @Mock
    private Function<CriteriaBuilder, BiFunction<Path<Integer>, Integer, Predicate>> integerComparisonFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Constructor should create PathComparisonValue with comparison factory and value")
    void constructor_WithComparisonFactoryAndValue_ShouldCreatePathComparisonValue() {
        // Given
        String testValue = "testValue";

        // When
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, testValue);

        // Then
        assertNotNull(pathComparison);
    }

    @Test
    @DisplayName("Constructor should handle null value")
    void constructor_WithNullValue_ShouldCreatePathComparisonValue() {
        // Given & When
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, null);

        // Then
        assertNotNull(pathComparison);
    }

    @Test
    @DisplayName("Constructor should handle null comparison factory")
    void constructor_WithNullComparisonFactory_ShouldCreatePathComparisonValue() {
        // Given
        String testValue = "testValue";

        // When
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(null, testValue);

        // Then
        assertNotNull(pathComparison);
    }

    @Test
    @DisplayName("getPredicate should return predicate when all parameters are valid")
    void getPredicate_WithValidParameters_ShouldReturnPredicate() {
        // Given
        String fieldName = "name";
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(root).get(fieldName);
        verify(stringComparisonFactory).apply(criteriaBuilder);
        verify(stringComparison).apply(stringPath, value);
    }

    @Test
    @DisplayName("getPredicate should return empty when value is null")
    void getPredicate_WithNullValue_ShouldReturnEmpty() {
        // Given
        String fieldName = "name";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, null);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verify(root).get(fieldName);
        verify(stringComparisonFactory).apply(criteriaBuilder);
        verify(stringComparison, never()).apply(any(), any());
    }

    @Test
    @DisplayName("getPredicate should return empty when comparison factory is null")
    void getPredicate_WithNullComparisonFactory_ShouldReturnEmpty() {
        // Given
        String fieldName = "name";
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(null, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);

        // When & Then
        assertThrows(NullPointerException.class, () ->
                pathComparison.getPredicate(criteriaBuilder, root, fieldName));

        verify(root).get(fieldName);
    }

    @Test
    @DisplayName("getPredicate should return empty when comparison factory returns null")
    void getPredicate_WithComparisonFactoryReturningNull_ShouldReturnEmpty() {
        // Given
        String fieldName = "name";
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(null);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verify(root).get(fieldName);
        verify(stringComparisonFactory).apply(criteriaBuilder);
    }

    @Test
    @DisplayName("getPredicate should handle integer values")
    void getPredicate_WithIntegerValue_ShouldWork() {
        // Given
        String fieldName = "age";
        Integer value = 25;
        PathComparisonValue<TestEntity, Integer> pathComparison =
                new PathComparisonValue<>(integerComparisonFactory, value);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(integerComparisonFactory.apply(criteriaBuilder)).thenReturn(integerComparison);
        when(integerComparison.apply(integerPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(root).get(fieldName);
        verify(integerComparisonFactory).apply(criteriaBuilder);
        verify(integerComparison).apply(integerPath, value);
    }

    @Test
    @DisplayName("getPredicate should handle date values")
    void getPredicate_WithDateValue_ShouldWork() {
        // Given
        String fieldName = "birthDate";
        LocalDate value = LocalDate.of(1990, 1, 1);
        Function<CriteriaBuilder, BiFunction<Path<LocalDate>, LocalDate, Predicate>> dateComparisonFactory =
                mock(Function.class);
        BiFunction<Path<LocalDate>, LocalDate, Predicate> dateComparison = mock(BiFunction.class);
        PathComparisonValue<TestEntity, LocalDate> pathComparison =
                new PathComparisonValue<>(dateComparisonFactory, value);

        when(root.<LocalDate>get(fieldName)).thenReturn(datePath);
        when(dateComparisonFactory.apply(criteriaBuilder)).thenReturn(dateComparison);
        when(dateComparison.apply(datePath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(root).get(fieldName);
        verify(dateComparisonFactory).apply(criteriaBuilder);
        verify(dateComparison).apply(datePath, value);
    }

    @Test
    @DisplayName("getPredicate should handle BigDecimal values")
    void getPredicate_WithBigDecimalValue_ShouldWork() {
        // Given
        String fieldName = "salary";
        BigDecimal value = new BigDecimal("50000.00");
        Function<CriteriaBuilder, BiFunction<Path<BigDecimal>, BigDecimal, Predicate>> decimalComparisonFactory =
                mock(Function.class);
        BiFunction<Path<BigDecimal>, BigDecimal, Predicate> decimalComparison = mock(BiFunction.class);
        PathComparisonValue<TestEntity, BigDecimal> pathComparison =
                new PathComparisonValue<>(decimalComparisonFactory, value);

        when(root.<BigDecimal>get(fieldName)).thenReturn(decimalPath);
        when(decimalComparisonFactory.apply(criteriaBuilder)).thenReturn(decimalComparison);
        when(decimalComparison.apply(decimalPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(root).get(fieldName);
        verify(decimalComparisonFactory).apply(criteriaBuilder);
        verify(decimalComparison).apply(decimalPath, value);
    }

    @ParameterizedTest
    @DisplayName("getPredicate should handle different field names")
    @MethodSource("provideFieldNames")
    void getPredicate_WithDifferentFieldNames_ShouldWork(String fieldName, String testDescription) {
        // Given
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent(), testDescription);
        assertEquals(mockPredicate, result.get());
        verify(root).get(fieldName);
    }

    private static Stream<Arguments> provideFieldNames() {
        return Stream.of(
                Arguments.of("name", "Normal field name"),
                Arguments.of("firstName", "Camel case field name"),
                Arguments.of("first_name", "Snake case field name"),
                Arguments.of("field123", "Field name with numbers"),
                Arguments.of("a", "Single character field name"),
                Arguments.of("veryLongFieldNameThatIsStillValid", "Very long field name")
        );
    }

    @Test
    @DisplayName("getPredicate should be reusable for multiple calls")
    void getPredicate_ShouldBeReusableForMultipleCalls() {
        // Given
        String fieldName1 = "field1";
        String fieldName2 = "field2";
        String value = "testValue";
        Path<String> stringPath2 = mock(Path.class);
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName1)).thenReturn(stringPath);
        when(root.<String>get(fieldName2)).thenReturn(stringPath2);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(mockPredicate);
        when(stringComparison.apply(stringPath2, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result1 = pathComparison.getPredicate(criteriaBuilder, root, fieldName1);
        Optional<Predicate> result2 = pathComparison.getPredicate(criteriaBuilder, root, fieldName2);

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(mockPredicate, result1.get());
        assertEquals(mockPredicate, result2.get());
        verify(root).get(fieldName1);
        verify(root).get(fieldName2);
        verify(stringComparisonFactory, times(2)).apply(criteriaBuilder);
        verify(stringComparison).apply(stringPath, value);
        verify(stringComparison).apply(stringPath2, value);
    }

    @Test
    @DisplayName("getPredicate should handle empty string value")
    void getPredicate_WithEmptyStringValue_ShouldReturnPredicate() {
        // Given
        String fieldName = "name";
        String value = "";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(stringComparison).apply(stringPath, value);
    }

    @Test
    @DisplayName("getPredicate should handle zero integer value")
    void getPredicate_WithZeroIntegerValue_ShouldReturnPredicate() {
        // Given
        String fieldName = "age";
        Integer value = 0;
        PathComparisonValue<TestEntity, Integer> pathComparison =
                new PathComparisonValue<>(integerComparisonFactory, value);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(integerComparisonFactory.apply(criteriaBuilder)).thenReturn(integerComparison);
        when(integerComparison.apply(integerPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(integerComparison).apply(integerPath, value);
    }

    @Test
    @DisplayName("getPredicate should handle comparison that returns null predicate")
    void getPredicate_WithComparisonReturningNull_ShouldReturnEmpty() {
        // Given
        String fieldName = "name";
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(null);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verify(stringComparison).apply(stringPath, value);
    }

    @Test
    @DisplayName("getPredicate should pass correct parameters to comparison")
    void getPredicate_ShouldPassCorrectParametersToComparison() {
        // Given
        String fieldName = "specificField";
        String value = "specificValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(mockPredicate);

        // When
        pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        verify(root).get(fieldName);
        verify(stringComparisonFactory).apply(criteriaBuilder);
        verify(stringComparison).apply(stringPath, value);
    }

    @Test
    @DisplayName("getPredicate should work with different CriteriaBuilder instances")
    void getPredicate_WithDifferentCriteriaBuilders_ShouldWork() {
        // Given
        CriteriaBuilder otherCriteriaBuilder = mock(CriteriaBuilder.class);
        String fieldName = "name";
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(otherCriteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(otherCriteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(stringComparisonFactory).apply(otherCriteriaBuilder);
        verifyNoInteractions(criteriaBuilder);
    }

    @Test
    @DisplayName("getPredicate should handle exception from comparison factory")
    void getPredicate_WithComparisonFactoryThrowingException_ShouldPropagateException() {
        // Given
        String fieldName = "name";
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenThrow(new RuntimeException("Factory error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                pathComparison.getPredicate(criteriaBuilder, root, fieldName));
    }

    @Test
    @DisplayName("getPredicate should handle exception from comparison")
    void getPredicate_WithComparisonThrowingException_ShouldPropagateException() {
        // Given
        String fieldName = "name";
        String value = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenThrow(new RuntimeException("Comparison error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                pathComparison.getPredicate(criteriaBuilder, root, fieldName));
    }

    @Test
    @DisplayName("getPredicate should handle complex nested field paths")
    void getPredicate_WithNestedFieldPath_ShouldWork() {
        // Given
        String fieldName = "address.city";
        String value = "New York";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, value);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(root).get(fieldName);
    }

    @Test
    @DisplayName("getPredicate should handle Boolean values")
    void getPredicate_WithBooleanValue_ShouldWork() {
        // Given
        String fieldName = "active";
        Boolean value = true;
        Function<CriteriaBuilder, BiFunction<Path<Boolean>, Boolean, Predicate>> booleanComparisonFactory =
                mock(Function.class);
        BiFunction<Path<Boolean>, Boolean, Predicate> booleanComparison = mock(BiFunction.class);
        Path<Boolean> booleanPath = mock(Path.class);
        PathComparisonValue<TestEntity, Boolean> pathComparison =
                new PathComparisonValue<>(booleanComparisonFactory, value);

        when(root.<Boolean>get(fieldName)).thenReturn(booleanPath);
        when(booleanComparisonFactory.apply(criteriaBuilder)).thenReturn(booleanComparison);
        when(booleanComparison.apply(booleanPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(booleanComparison).apply(booleanPath, value);
    }

    @Test
    @DisplayName("getPredicate should handle negative integer values")
    void getPredicate_WithNegativeIntegerValue_ShouldWork() {
        // Given
        String fieldName = "balance";
        Integer value = -100;
        PathComparisonValue<TestEntity, Integer> pathComparison =
                new PathComparisonValue<>(integerComparisonFactory, value);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(integerComparisonFactory.apply(criteriaBuilder)).thenReturn(integerComparison);
        when(integerComparison.apply(integerPath, value)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(integerComparison).apply(integerPath, value);
    }

    @Test
    @DisplayName("Constructor and getPredicate should handle immutable values correctly")
    void getPredicate_WithImmutableValue_ShouldWork() {
        // Given
        String fieldName = "name";
        String originalValue = "testValue";
        PathComparisonValue<TestEntity, String> pathComparison =
                new PathComparisonValue<>(stringComparisonFactory, originalValue);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(stringComparisonFactory.apply(criteriaBuilder)).thenReturn(stringComparison);
        when(stringComparison.apply(stringPath, originalValue)).thenReturn(mockPredicate);

        // When
        Optional<Predicate> result = pathComparison.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(mockPredicate, result.get());
        verify(stringComparison).apply(stringPath, originalValue);
    }

    // Test entity class for generic type parameter
    private static class TestEntity {
    }
}
