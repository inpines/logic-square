package org.dotspace.oofp.support.orm.specification.expression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PathRangeBetweenTest {

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Root<TestEntity> root;

    @Mock
    private Path<Integer> integerPath;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<BigDecimal> decimalPath;

    @Mock
    private Predicate betweenPredicate;

    @Mock
    private Predicate greaterThanPredicate;

    @Test
    @DisplayName("Constructor with both low and high values should create valid range")
    void constructor_WithBothLowAndHigh_ShouldCreateValidRange() {
        // Given
        Integer low = 10;
        Integer high = 20;

        // When
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        // Then
        assertNotNull(pathRangeBetween);
    }

    @Test
    @DisplayName("Constructor with null low value should handle gracefully")
    void constructor_WithNullLow_ShouldHandleGracefully() {
        // Given
        Integer low = null;
        Integer high = 20;

        // When
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        // Then
        assertNotNull(pathRangeBetween);
    }

    @Test
    @DisplayName("Constructor with null high value should handle gracefully")
    void constructor_WithNullHigh_ShouldHandleGracefully() {
        // Given
        Integer low = 10;
        Integer high = null;

        // When
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        // Then
        assertNotNull(pathRangeBetween);
    }

    @Test
    @DisplayName("Constructor with both null values should handle gracefully")
    void constructor_WithBothNull_ShouldHandleGracefully() {
        // Given
        Integer low = null;
        Integer high = null;

        // When
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        // Then
        assertNotNull(pathRangeBetween);
    }

    @Test
    @DisplayName("Constructor with only high value should create pair with same value for both")
    void constructor_WithOnlyHigh_ShouldCreatePairWithSameValue() {
        // Given
        Integer low = null;
        Integer high = 15;

        // When
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        // Then
        assertNotNull(pathRangeBetween);
    }

    @Test
    @DisplayName("getPredicate with valid range should return between predicate")
    void getPredicate_WithValidRange_ShouldReturnBetweenPredicate() {
        // Given
        Integer low = 10;
        Integer high = 20;
        String fieldName = "age";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(integerPath, low, high);
        verify(criteriaBuilder, never()).lessThanOrEqualTo(any(Path.class), any(Integer.class));
        verify(criteriaBuilder, never()).greaterThan(any(Path.class), any(Integer.class));
    }

    @Test
    @DisplayName("getPredicate with null high should return greaterThan predicate")
    void getPredicate_WithNullHigh_ShouldReturnGreaterThanPredicate() {
        // Given
        Integer low = 10;
        Integer high = null;
        String fieldName = "age";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.greaterThan(integerPath, low)).thenReturn(greaterThanPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(greaterThanPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).greaterThan(integerPath, low);
        verify(criteriaBuilder, never()).between(any(Path.class), any(Integer.class), any(Integer.class));
        verify(criteriaBuilder, never()).lessThanOrEqualTo(any(Path.class), any(Integer.class));
    }

    @Test
    @DisplayName("getPredicate with both null values should return empty optional")
    void getPredicate_WithBothNull_ShouldReturnEmptyOptional() {
        // Given
        Integer low = null;
        Integer high = null;
        String fieldName = "age";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertFalse(result.isPresent());
        verify(root, never()).get(anyString());
        verify(criteriaBuilder, never()).between(any(Path.class), any(Integer.class), any(Integer.class));
        verify(criteriaBuilder, never()).lessThanOrEqualTo(any(Path.class), any(Integer.class));
        verify(criteriaBuilder, never()).greaterThan(any(Path.class), any(Integer.class));
    }

    @Test
    @DisplayName("getPredicate with only high value should return between predicate with same values")
    void getPredicate_WithOnlyHigh_ShouldReturnBetweenPredicateWithSameValues() {
        // Given
        Integer low = null;
        Integer high = 15;
        String fieldName = "score";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, high, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(integerPath, high, high);
    }

    @Test
    @DisplayName("getPredicate should work with String type")
    void getPredicate_WithStringType_ShouldWork() {
        // Given
        String low = "apple";
        String high = "zebra";
        String fieldName = "name";
        PathRangeBetween<TestEntity, String> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<String>get(fieldName)).thenReturn(stringPath);
        when(criteriaBuilder.between(stringPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(stringPath, low, high);
    }

    @Test
    @DisplayName("getPredicate should work with BigDecimal type")
    void getPredicate_WithBigDecimalType_ShouldWork() {
        // Given
        BigDecimal low = new BigDecimal("10.50");
        BigDecimal high = new BigDecimal("99.99");
        String fieldName = "price";
        PathRangeBetween<TestEntity, BigDecimal> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<BigDecimal>get(fieldName)).thenReturn(decimalPath);
        when(criteriaBuilder.between(decimalPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(decimalPath, low, high);
    }

    @ParameterizedTest
    @DisplayName("getPredicate should handle different integer value scenarios")
    @MethodSource("provideIntegerTestScenarios")
    void getPredicate_WithDifferentIntegerScenarios_ShouldReturnBetweenPredicate(Integer low, Integer high, String fieldName, String testDescription) {
        // Given
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent(), testDescription);
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(integerPath, low, high);
    }

    private static Stream<Arguments> provideIntegerTestScenarios() {
        return Stream.of(
                Arguments.of(50, 50, "exactValue", "Equal low and high values test"),
                Arguments.of(100, 50, "reversedRange", "Reversed range (high < low) test"),
                Arguments.of(0, 0, "zeroField", "Zero values test"),
                Arguments.of(-100, -10, "negativeField", "Negative values test")
        );
    }

    @Test
    @DisplayName("getPredicate should handle reversed range (high < low)")
    void getPredicate_WithReversedRange_ShouldStillCreateBetweenPredicate() {
        // Given
        Integer low = 100;
        Integer high = 50;
        String fieldName = "reversedRange";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(integerPath, low, high);
    }

    @Test
    @DisplayName("getPredicate should work with zero values")
    void getPredicate_WithZeroValues_ShouldWork() {
        // Given
        Integer low = 0;
        Integer high = 0;
        String fieldName = "zeroField";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(integerPath, low, high);
    }

    @Test
    @DisplayName("getPredicate should work with negative values")
    void getPredicate_WithNegativeValues_ShouldWork() {
        // Given
        Integer low = -100;
        Integer high = -10;
        String fieldName = "negativeField";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(betweenPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(integerPath, low, high);
    }

    @Test
    @DisplayName("getPredicate should work with positive low and null high")
    void getPredicate_WithPositiveLowAndNullHigh_ShouldReturnGreaterThan() {
        // Given
        Integer low = 50;
        Integer high = null;
        String fieldName = "lowerBound";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.greaterThan(integerPath, low)).thenReturn(greaterThanPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(greaterThanPredicate, result.get());
        verify(root).get(fieldName);
        verify(criteriaBuilder).greaterThan(integerPath, low);
    }

    @Test
    @DisplayName("getPredicate should handle different field names")
    void getPredicate_WithDifferentFieldNames_ShouldWork() {
        // Given
        Integer low = 1;
        Integer high = 10;
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get("field1")).thenReturn(integerPath);
        when(root.<Integer>get("field2")).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result1 = pathRangeBetween.getPredicate(criteriaBuilder, root, "field1");
        Optional<Predicate> result2 = pathRangeBetween.getPredicate(criteriaBuilder, root, "field2");

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        verify(root).get("field1");
        verify(root).get("field2");
        verify(criteriaBuilder, times(2)).between(integerPath, low, high);
    }

    @Test
    @DisplayName("getPredicate should handle empty string field name")
    void getPredicate_WithEmptyStringFieldName_ShouldCallRootGet() {
        // Given
        Integer low = 5;
        Integer high = 15;
        String fieldName = "";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName);

        // Then
        assertTrue(result.isPresent());
        verify(root).get(fieldName);
        verify(criteriaBuilder).between(integerPath, low, high);
    }

    @Test
    @DisplayName("PathRangeBetween should be reusable for multiple predicate generations")
    void pathRangeBetween_ShouldBeReusableForMultiplePredications() {
        // Given
        Integer low = 25;
        Integer high = 75;
        String fieldName1 = "score1";
        String fieldName2 = "score2";
        PathRangeBetween<TestEntity, Integer> pathRangeBetween = new PathRangeBetween<>(low, high);

        when(root.<Integer>get(fieldName1)).thenReturn(integerPath);
        when(root.<Integer>get(fieldName2)).thenReturn(integerPath);
        when(criteriaBuilder.between(integerPath, low, high)).thenReturn(betweenPredicate);

        // When
        Optional<Predicate> result1 = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName1);
        Optional<Predicate> result2 = pathRangeBetween.getPredicate(criteriaBuilder, root, fieldName2);

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(betweenPredicate, result1.get());
        assertEquals(betweenPredicate, result2.get());
        verify(root).get(fieldName1);
        verify(root).get(fieldName2);
        verify(criteriaBuilder, times(2)).between(integerPath, low, high);
    }

    // Test entity class for generic type parameter
    private static class TestEntity {
    }
}
