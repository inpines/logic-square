package org.dotspace.oofp.utils.functional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PredicatesTest {

    // ---------- ok() ----------
    @Test
    @DisplayName("ok(): 永遠回 true")
    void ok_alwaysTrue() {
        Predicate<String> p = Predicates.ok();
        assertTrue(p.test(null));
        assertTrue(p.test(""));
        assertTrue(p.test("x"));
        assertTrue(Predicates.<Integer>ok().test(123));
    }

    // ---------- isPresent() ----------
    @Test
    @DisplayName("isPresent(): null 與空白字串為 false；非空白字串為 true")
    void isPresent_nullAndBlank() {
        Predicate<String> p = Predicates.isPresent();

        assertFalse(p.test(null));
        assertFalse(p.test(""));
        assertFalse(p.test("   "));
        assertTrue(p.test("a"));
        assertTrue(p.test("  a "));
    }

    @Test
    @DisplayName("isPresent(): 非 String 型別且非 null -> true")
    void isPresent_nonStringNonNull_isTrue() {
        Predicate<Object> p = Predicates.isPresent();

        assertTrue(p.test(0));                 // Integer 0
        assertTrue(p.test(new Object()));      // 任意物件
        assertTrue(p.test(List.of()));         // 空集合亦為 true（實作未特判集合）
    }

    // ---------- not(...) ----------
    @Test
    @DisplayName("not(): 否定邏輯")
    void not_negate() {
        Predicate<String> present = Predicates.isPresent();
        Predicate<String> notPresent = Predicates.not(present);

        assertTrue(notPresent.test("   "));
        assertTrue(notPresent.test(null));
        assertFalse(notPresent.test("x"));
    }

    // ---------- isNotPresent() ----------
    @Test
    @DisplayName("isNotPresent(): 回傳與 isPresent 相反的 Predicate（忽略 apply 的參數）")
    void isNotPresent_functionProducesNegatedPredicate() {
        Function<Object, Predicate<String>> f = Predicates.isNotPresent();

        Predicate<String> np = f.apply(new Object()); // 參數無實質用途
        assertTrue(np.test(null));
        assertTrue(np.test(""));
        assertTrue(np.test("   "));
        assertFalse(np.test("x"));
    }

    // ---------- of(...) ----------
    @Test
    @DisplayName("of(): 回傳傳入的 Predicate 本體")
    void of_identity() {
        Predicate<Integer> gt5 = i -> i != null && i > 5;
        Predicate<Integer> same = Predicates.of(gt5);

        assertTrue(same.test(6));
        assertFalse(same.test(5));
        assertFalse(same.test(null));
        assertSame(gt5, same);
    }

    // ---------- any(...) ----------
    @Test
    @DisplayName("any(): 目標值等於任一清單元素為 true")
    void any_matchesAny() {
        Predicate<String> p = Predicates.any(List.of("a", "b", "c"));
        assertTrue(p.test("a"));
        assertTrue(p.test("c"));
        assertFalse(p.test("x"));

        Predicate<Integer> q = Predicates.any(List.of(1, 2, 3, 3));
        assertTrue(q.test(3));
        assertFalse(q.test(4));
    }

    // ---------- all(...) ----------
    @Test
    @DisplayName("all(): 目標值等於清單所有元素為 true；空清單視為 true")
    void all_matchesAll_orEmptyIsTrue() {
        Predicate<String> p1 = Predicates.all(List.of("x", "x", "x"));
        assertTrue(p1.test("x"));
        assertFalse(p1.test("y"));

        Predicate<Integer> p2 = Predicates.all(List.of());
        assertTrue(p2.test(999)); // allMatch on empty stream -> true
    }

    // ---------- present(reader) ----------
    static class Bean {
        final String name;
        Bean(String name) { this.name = name; }
        String getName() { return name; }
    }

    @Test
    @DisplayName("present(reader): 讀取值非空白 -> true；空白 -> false")
    void present_reader_nonBlankTrueBlankFalse() {
        Predicate<Bean> p = Predicates.present(Bean::getName);

        assertTrue(p.test(new Bean("Alice")));
        assertFalse(p.test(new Bean("")));
        assertFalse(p.test(new Bean("   ")));
    }

    @Test
    @DisplayName("present(reader): 傳入 null 時丟 NullPointerException（因 Optional.of(t)）")
    void present_reader_nullInput_throwsNpe() {
        Predicate<Bean> p = Predicates.present(Bean::getName);
        assertThrows(NullPointerException.class, () -> p.test(null));
    }

    // ---------- between(start,end) ----------
    @Test
    @DisplayName("between(): 整數含邊界")
    void between_integer_inclusive() {
        Predicate<Integer> p = Predicates.between(10, 20);
        assertFalse(p.test(9));
        assertTrue(p.test(10));
        assertTrue(p.test(15));
        assertTrue(p.test(20));
        assertFalse(p.test(21));
    }

    @Test
    @DisplayName("between(): 字串按字典順序且含邊界")
    void between_string_lexicographic_inclusive() {
        Predicate<String> p = Predicates.between("b", "d");
        assertFalse(p.test("a"));
        assertTrue(p.test("b"));
        assertTrue(p.test("c"));
        assertTrue(p.test("d"));
        assertFalse(p.test("e"));
    }
}
