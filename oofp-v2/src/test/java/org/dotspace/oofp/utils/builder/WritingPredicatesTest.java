package org.dotspace.oofp.utils.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WritingPredicatesTest {

    // === ifPresent ===

    @Test
    @DisplayName("ifPresent(value) — value 非 null 時應為 true")
    void ifPresent_nonNull_shouldPass() {
        var p = WritingPredicates.ifPresent("hello");
        assertTrue(p.test(new Object())); // value 非 null 時皆斷言成功
        assertFalse(p.test(null)); // null 則斷言失敗
    }

    @Test
    @DisplayName("ifPresent(value) — value 為 null 時應為 false")
    void ifPresent_null_shouldFail() {
        var p = WritingPredicates.<Object, String>ifPresent(null);
        assertFalse(p.test(new Object()));
        assertFalse(p.test(null));
    }

    // === ifMatch(predicate, constant) ===

    @Test
    @DisplayName("ifMatch(predicate, constant) — 常值匹配成功")
    void ifMatch_constant_shouldPass() {
        Predicate<String> startsWithA = s -> s != null && s.startsWith("A");
        var p = WritingPredicates.ifMatch(startsWithA, "ABC");
        assertTrue(p.test(new Object()));
    }

    @Test
    @DisplayName("ifMatch(predicate, constant) — 常值匹配失敗")
    void ifMatch_constant_shouldFail() {
        Predicate<String> startsWithA = s -> s != null && s.startsWith("A");
        var p = WritingPredicates.ifMatch(startsWithA, "XYZ");
        assertFalse(p.test(new Object()));
    }

    // === ifMatch(predicate, selector) ===

    @Test
    @DisplayName("ifMatch(predicate, selector) — 以 selector 取 state 判斷（成功/失敗）")
    void ifMatch_selector_shouldWork() {
        Predicate<Integer> isEven = i -> i != null && i % 2 == 0;
        var p = WritingPredicates.ifApplierMatch(isEven, String::length);

        assertTrue(p.test("abcd")); // 4 偶數
        assertFalse(p.test("abc")); // 3 奇數
    }

    // === null predicate 行為 ===

    @Test
    @DisplayName("當 predicate 為 null 時，test(...) 應永遠回 true")
    void nullPredicate_shouldAlwaysTrue() {
        var p = new WritingPredicates<>(null, String::length);
        assertTrue(p.test("x"));
        assertTrue(p.test(null));
    }

    // === null instance 與 selector 呼叫次數 ===

    @Test
    @DisplayName("instance 為 null 時，不應呼叫 stateSelector")
    void nullInstance_shouldNotCallSelector() {
        @SuppressWarnings("unchecked")
        Function<Object, String> selector = mock(Function.class);
        Predicate<String> nonEmpty = s -> s != null && !s.isEmpty();

        var p = new WritingPredicates<>(nonEmpty, selector);

        assertFalse(p.test(null));
        verify(selector, never()).apply(any());
    }

    @Test
    @DisplayName("instance 非 null 時，應呼叫 stateSelector 一次")
    void nonNullInstance_shouldCallSelectorOnce() {
        @SuppressWarnings("unchecked")
        Function<Object, Integer> selector = mock(Function.class);
        when(selector.apply(any())).thenReturn(10);

        Predicate<Integer> gt5 = i -> i != null && i > 5;
        var p = new WritingPredicates<>(gt5, selector);

        assertTrue(p.test(new Object()));
        verify(selector, times(1)).apply(any());
    }
}
