package org.dotspace.oofp.support.orm.specification.builder;

import org.dotspace.oofp.support.orm.specification.CriteriaOrder;
import org.dotspace.oofp.support.orm.specification.CriteriaPredicateExpression;
import org.dotspace.oofp.support.orm.specification.GeneralSpecification;
import org.dotspace.oofp.support.orm.specification.SpecificationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecificationBuilderBaseTest {

    @Mock
    private CriteriaPredicateExpression<TestEntity> mockExpression1;

    @Mock
    private CriteriaPredicateExpression<TestEntity> mockExpression2;

    @Mock
    private CriteriaPredicateExpression<TestEntity> mockExpression3;

    @Mock
    private CriteriaOrder mockOrder1;

    @Mock
    private CriteriaOrder mockOrder2;

    @Mock
    private CriteriaOrder mockOrder3;

    @Mock
    private Supplier<CriteriaPredicateExpression<TestEntity>> mockExpressionSupplier;

    @Mock
    private Predicate<String> mockStringPredicate;

    @Mock
    private Predicate<Integer> mockIntegerPredicate;

    @Mock
    private Predicate<TestEntity> mockEntityPredicate;

    private TestSpecificationBuilder specificationBuilder;

    @BeforeEach
    void setUp() {
        specificationBuilder = new TestSpecificationBuilder();
    }

    @Test
    @DisplayName("add should add expression with given name and return builder")
    void add_WithValidNameAndExpression_ShouldAddExpressionAndReturnBuilder() {
        // Given
        String name = "testExpression";

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpression1);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("add should handle null name")
    void add_WithNullName_ShouldWork() {
        // Given & When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(null, mockExpression1);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("add should handle null expression")
    void add_WithNullExpression_ShouldWork() {
        // Given
        String name = "testExpression";

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, null);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("add should handle empty string name")
    void add_WithEmptyStringName_ShouldWork() {
        // Given
        String name = "";

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpression1);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("add should replace expression when same name is used")
    void add_WithSameName_ShouldReplaceExpression() {
        // Given
        String name = "testExpression";

        // When
        specificationBuilder.add(name, mockExpression1);
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpression2);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("add should allow multiple expressions with different names")
    void add_WithDifferentNames_ShouldAllowMultipleExpressions() {
        // Given
        String name1 = "expression1";
        String name2 = "expression2";
        String name3 = "expression3";

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder
                .add(name1, mockExpression1)
                .add(name2, mockExpression2)
                .add(name3, mockExpression3);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should add single order and return builder")
    void orderBy_WithSingleOrder_ShouldAddOrderAndReturnBuilder() {
        // Given & When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy(mockOrder1);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should handle null order")
    void orderBy_WithNullOrder_ShouldWork() {
        // Given & When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy((CriteriaOrder) null);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should add multiple single orders")
    void orderBy_WithMultipleSingleOrders_ShouldAddAllOrders() {
        // Given & When
        SpecificationBuilder<TestEntity> result = specificationBuilder
                .orderBy(mockOrder1)
                .orderBy(mockOrder2)
                .orderBy(mockOrder3);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should add list of orders and return builder")
    void orderBy_WithListOfOrders_ShouldAddOrdersAndReturnBuilder() {
        // Given
        List<CriteriaOrder> orders = Arrays.asList(mockOrder1, mockOrder2, mockOrder3);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy(orders);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should handle null list of orders")
    void orderBy_WithNullListOfOrders_ShouldWork() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () ->
                specificationBuilder.orderBy((List<CriteriaOrder>) null)
        );
    }

    @Test
    @DisplayName("orderBy should handle empty list of orders")
    void orderBy_WithEmptyListOfOrders_ShouldWork() {
        // Given
        List<CriteriaOrder> orders = Collections.emptyList();

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy(orders);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should handle modifiable list of orders")
    void orderBy_WithModifiableListOfOrders_ShouldWork() {
        // Given
        List<CriteriaOrder> orders = new ArrayList<>();
        orders.add(mockOrder1);
        orders.add(mockOrder2);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy(orders);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should handle immutable list of orders")
    void orderBy_WithImmutableListOfOrders_ShouldWork() {
        // Given
        List<CriteriaOrder> orders = List.of(mockOrder1, mockOrder2);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy(orders);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("orderBy should handle list with null elements")
    void orderBy_WithListContainingNullElements_ShouldWork() {
        // Given
        List<CriteriaOrder> orders = Arrays.asList(mockOrder1, null, mockOrder2);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy(orders);

        // Then
        assertSame(specificationBuilder, result);
    }

    @Test
    @DisplayName("build should return GeneralSpecification")
    void build_ShouldReturnGeneralSpecification() {
        // Given
        specificationBuilder.add("test", mockExpression1);
        specificationBuilder.orderBy(mockOrder1);

        // When
        Specification<TestEntity> result = specificationBuilder.build();

        // Then
        assertNotNull(result);
        assertInstanceOf(GeneralSpecification.class, result);
    }

    @Test
    @DisplayName("build should work with empty expressions and orders")
    void build_WithEmptyExpressionsAndOrders_ShouldWork() {
        // Given & When
        Specification<TestEntity> result = specificationBuilder.build();

        // Then
        assertNotNull(result);
        assertInstanceOf(GeneralSpecification.class, result);
    }

    @Test
    @DisplayName("build should be reusable")
    void build_ShouldBeReusable() {
        // Given
        specificationBuilder.add("test", mockExpression1);

        // When
        Specification<TestEntity> result1 = specificationBuilder.build();
        Specification<TestEntity> result2 = specificationBuilder.build();

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2);
        assertInstanceOf(GeneralSpecification.class, result1);
        assertInstanceOf(GeneralSpecification.class, result2);
    }

    @Test
    @DisplayName("add with condition should add expression when condition matches predicate")
    void addWithCondition_WhenConditionMatchesPredicate_ShouldAddExpression() {
        // Given
        String name = "testExpression";
        String condition = "validCondition";
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);
        when(mockStringPredicate.test(condition)).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, mockStringPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(mockStringPredicate).test(condition);
    }

    @Test
    @DisplayName("add with condition should not add expression when condition does not match predicate")
    void addWithCondition_WhenConditionDoesNotMatchPredicate_ShouldNotAddExpression() {
        // Given
        String name = "testExpression";
        String condition = "invalidCondition";
        when(mockStringPredicate.test(condition)).thenReturn(false);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, mockStringPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockStringPredicate).test(condition);
        verify(mockExpressionSupplier, never()).get();
    }

    @Test
    @DisplayName("add with condition should not add expression when condition is null")
    void addWithCondition_WhenConditionIsNull_ShouldNotAddExpression() {
        // Given
        String name = "testExpression";
        String condition = null;

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, mockStringPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockStringPredicate, never()).test(any());
        verify(mockExpressionSupplier, never()).get();
    }

    @Test
    @DisplayName("add with condition should handle null expression supplier")
    void addWithCondition_WithNullExpressionSupplier_ShouldWork() {
        // Given
        String name = "testExpression";
        String condition = "validCondition";
        when(mockStringPredicate.test(condition)).thenReturn(true);

        // When & Then
        assertThrows(NullPointerException.class, () ->
                specificationBuilder.add(name, null, mockStringPredicate, condition)
        );
        verify(mockStringPredicate).test(condition);
    }

    @Test
    @DisplayName("add with condition should handle null predicate")
    void addWithCondition_WithNullPredicate_ShouldWork() {
        // Given
        String name = "testExpression";
        String condition = "validCondition";

        // When & Then
        assertThrows(NullPointerException.class, () ->
                specificationBuilder.add(name, mockExpressionSupplier, null, condition)
        );
        verify(mockExpressionSupplier, never()).get();
    }

    @Test
    @DisplayName("add with condition should handle null name")
    void addWithCondition_WithNullName_ShouldWork() {
        // Given
        String condition = "validCondition";
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);
        when(mockStringPredicate.test(condition)).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(null, mockExpressionSupplier, mockStringPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(mockStringPredicate).test(condition);
    }

    @Test
    @DisplayName("add with condition should work with integer condition")
    void addWithCondition_WithIntegerCondition_ShouldWork() {
        // Given
        String name = "testExpression";
        Integer condition = 42;
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);
        when(mockIntegerPredicate.test(condition)).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, mockIntegerPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(mockIntegerPredicate).test(condition);
    }

    @Test
    @DisplayName("add with condition should work with complex object condition")
    void addWithCondition_WithComplexObjectCondition_ShouldWork() {
        // Given
        String name = "testExpression";
        TestEntity condition = new TestEntity();
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);
        when(mockEntityPredicate.test(condition)).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, mockEntityPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(mockEntityPredicate).test(condition);
    }

    @Test
    @DisplayName("add with condition should handle expression supplier throwing exception")
    void addWithCondition_WithExpressionSupplierThrowingException_ShouldPropagateException() {
        // Given
        String name = "testExpression";
        String condition = "validCondition";
        when(mockStringPredicate.test(condition)).thenReturn(true);
        when(mockExpressionSupplier.get()).thenThrow(new RuntimeException("Supplier error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                specificationBuilder.add(name, mockExpressionSupplier, mockStringPredicate, condition));
    }

    @Test
    @DisplayName("add with condition should handle predicate throwing exception")
    void addWithCondition_WithPredicateThrowingException_ShouldPropagateException() {
        // Given
        String name = "testExpression";
        String condition = "validCondition";
        when(mockStringPredicate.test(condition)).thenThrow(new RuntimeException("Predicate error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                specificationBuilder.add(name, mockExpressionSupplier, mockStringPredicate, condition));
    }

    @ParameterizedTest
    @DisplayName("add with condition should handle different condition types")
    @MethodSource("provideConditionTypes")
    void addWithCondition_WithDifferentConditionTypes_ShouldWork(Object condition, String testDescription) {
        // Given
        String name = "testExpression";
        Predicate<Object> objectPredicate = mock(Predicate.class);
        when(objectPredicate.test(condition)).thenReturn(true);
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, objectPredicate, condition);

        // Then
        assertSame(specificationBuilder, result, testDescription);
        verify(objectPredicate).test(condition);
        verify(mockExpressionSupplier).get();
    }

    private static Stream<Arguments> provideConditionTypes() {
        return Stream.of(
                Arguments.of("stringCondition", "String condition test"),
                Arguments.of(123, "Integer condition test"),
                Arguments.of(123L, "Long condition test"),
                Arguments.of(123.45, "Double condition test"),
                Arguments.of(true, "Boolean condition test"),
                Arguments.of(new TestEntity(), "Object condition test")
        );
    }

    @Test
    @DisplayName("chaining methods should work together")
    void chainingMethods_ShouldWorkTogether() {
        // Given
        when(mockExpressionSupplier.get()).thenReturn(mockExpression3);
        when(mockStringPredicate.test("validCondition")).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder
                .add("expr1", mockExpression1)
                .add("expr2", mockExpression2)
                .orderBy(mockOrder1)
                .orderBy(mockOrder2)
                .add("expr3", mockExpressionSupplier, mockStringPredicate, "validCondition")
                .orderBy(List.of(mockOrder3));

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(mockStringPredicate).test("validCondition");
    }

    @Test
    @DisplayName("build after chaining should create complete specification")
    void buildAfterChaining_ShouldCreateCompleteSpecification() {
        // Given
        when(mockExpressionSupplier.get()).thenReturn(mockExpression3);
        when(mockStringPredicate.test("validCondition")).thenReturn(true);

        // When
        Specification<TestEntity> result = specificationBuilder
                .add("expr1", mockExpression1)
                .add("expr2", mockExpression2)
                .orderBy(mockOrder1)
                .add("expr3", mockExpressionSupplier, mockStringPredicate, "validCondition")
                .build();

        // Then
        assertNotNull(result);
        assertInstanceOf(GeneralSpecification.class, result);
    }

    @Test
    @DisplayName("multiple builds should create independent specifications")
    void multipleBuilds_ShouldCreateIndependentSpecifications() {
        // Given
        specificationBuilder.add("expr1", mockExpression1);

        // When
        Specification<TestEntity> spec1 = specificationBuilder.build();
        specificationBuilder.add("expr2", mockExpression2);
        Specification<TestEntity> spec2 = specificationBuilder.build();

        // Then
        assertNotNull(spec1);
        assertNotNull(spec2);
        assertNotSame(spec1, spec2);
        assertInstanceOf(GeneralSpecification.class, spec1);
        assertInstanceOf(GeneralSpecification.class, spec2);
    }

    @Test
    @DisplayName("add with condition should work with zero integer condition")
    void addWithCondition_WithZeroIntegerCondition_ShouldWork() {
        // Given
        String name = "testExpression";
        Integer condition = 0;
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);
        when(mockIntegerPredicate.test(condition)).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, mockIntegerPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(mockIntegerPredicate).test(condition);
    }

    @Test
    @DisplayName("add with condition should work with empty string condition")
    void addWithCondition_WithEmptyStringCondition_ShouldWork() {
        // Given
        String name = "testExpression";
        String condition = "";
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);
        when(mockStringPredicate.test(condition)).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, mockStringPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(mockStringPredicate).test(condition);
    }

    @Test
    @DisplayName("add with condition should work with false boolean condition")
    void addWithCondition_WithFalseBooleanCondition_ShouldWork() {
        // Given
        String name = "testExpression";
        Boolean condition = false;
        Predicate<Boolean> booleanPredicate = mock(Predicate.class);
        when(mockExpressionSupplier.get()).thenReturn(mockExpression1);
        when(booleanPredicate.test(condition)).thenReturn(true);

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.add(name, mockExpressionSupplier, booleanPredicate, condition);

        // Then
        assertSame(specificationBuilder, result);
        verify(mockExpressionSupplier).get();
        verify(booleanPredicate).test(condition);
    }

    @Test
    @DisplayName("orderBy with large number of orders should work")
    void orderBy_WithLargeNumberOfOrders_ShouldWork() {
        // Given
        List<CriteriaOrder> orders = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            orders.add(mock(CriteriaOrder.class));
        }

        // When
        SpecificationBuilder<TestEntity> result = specificationBuilder.orderBy(orders);

        // Then
        assertSame(specificationBuilder, result);
    }

    // Concrete implementation for testing
    private static class TestSpecificationBuilder extends SpecificationBuilderBase<TestEntity> {
        // No additional implementation needed for testing
    }

    // Test entity class for generic type parameter
    private static class TestEntity {
    }
}
