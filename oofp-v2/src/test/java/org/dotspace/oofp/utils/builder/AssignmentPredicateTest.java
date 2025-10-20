package org.dotspace.oofp.utils.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

class AssignmentPredicateTest {

    @Test
    void testIfPresent_nonNullValue_shouldPass() {
        AssignmentPredicate<String, String> predicate =
                AssignmentPredicate.ifPresent("hello");

        assertTrue(predicate.test("any"));   // 固定傳回 "hello"，非 null
    }

    @Test
    void testIfPresent_nullValue_shouldFail() {
        AssignmentPredicate<String, String> predicate =
                AssignmentPredicate.ifPresent(null);

        assertFalse(predicate.test("any"));  // 固定傳回 null，不通過 isPresent()
    }

    @Test
    void testIfMatch_constantValue_shouldPass() {
        Predicate<String> startsWithA = s -> s != null && s.startsWith("A");
        AssignmentPredicate<Object, String> predicate =
                AssignmentPredicate.ifMatch(startsWithA, "ABC");

        assertTrue(predicate.test(new Object()));
    }

    @Test
    void testIfMatch_constantValue_shouldFail() {
        Predicate<String> startsWithA = s -> s != null && s.startsWith("A");
        AssignmentPredicate<Object, String> predicate =
                AssignmentPredicate.ifMatch(startsWithA, "XYZ");

        assertFalse(predicate.test(new Object()));
    }

    @Test
    void testIfMatch_withSelector_shouldWork() {
        Predicate<Integer> isEven = i -> i != null && i % 2 == 0;
        AssignmentPredicate<String, Integer> predicate =
                AssignmentPredicate.ifApplierMatch(isEven, String::length);

        assertTrue(predicate.test("abcd"));  // 長度 4 是偶數
        assertFalse(predicate.test("abc"));  // 長度 3 是奇數
    }

    @Test
    void testNullPredicate_shouldAlwaysPass() {
        AssignmentPredicate<String, String> predicate =
                new AssignmentPredicate<>(null, String::toUpperCase);

        assertTrue(predicate.test("abc"));
        assertTrue(predicate.test(null));
    }

    @Test
    void testSelectorGetsNullInstance() {
        Function<Object, String> selector = mock(Function.class);
        Predicate<String> nonEmpty = s -> s != null && !s.isEmpty();

        AssignmentPredicate<Object, String> predicate =
                new AssignmentPredicate<>(nonEmpty, selector);

        // 當 instance 為 null，Optional.ofNullable(null) → orElse(null)，不會呼叫 selector
        assertFalse(predicate.test(null));
        verify(selector, never()).apply(any());
    }
}
