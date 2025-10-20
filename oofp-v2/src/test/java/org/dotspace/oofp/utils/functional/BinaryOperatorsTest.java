package org.dotspace.oofp.utils.functional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BinaryOperatorsTest {

    static class YourOps {
        public <T> BinaryOperator<T> first() { return (l, r) -> l; }
        public <T> BinaryOperator<T> last() { return (l, r) -> r; }
        public <T> BinaryOperator<List<T>> addAll() {
            return (c1, c2) -> { c1.addAll(c2); return c1; };
        }
        public <T extends Comparable<T>> BinaryOperator<T> min() {
            return (l, r) -> (l.compareTo(r) >= 0) ? r : l;
        }
        public <T extends Comparable<T>> BinaryOperator<T> max() {
            return (l, r) -> (l.compareTo(r) > 0) ? l : r;
        }
        public BinaryOperator<Integer> totalInteger() { return Integer::sum; }
        public BinaryOperator<Long> totalLong() { return Long::sum; }
    }

    private final YourOps ops = new YourOps();

    // ---------- first ----------
    @Test
    @DisplayName("first：回傳左操作元；在 reduce 中得到第一個元素")
    void first_returnsLeft_andReduceGetsFirst() {
        var first = ops.<String>first();
        assertEquals("L", first.apply("L", "R"));

        String reduced = Stream.of("a", "b", "c").reduce(first).orElse(null);
        assertEquals("a", reduced); // (a ◁ b) ◁ c => a
    }

    // ---------- last ----------
    @Test
    @DisplayName("last：回傳右操作元；在 reduce 中得到最後一個元素")
    void last_returnsRight_andReduceGetsLast() {
        var last = ops.<String>last();
        assertEquals("R", last.apply("L", "R"));

        String reduced = Stream.of("a", "b", "c").reduce(last).orElse(null);
        assertEquals("c", reduced); // (a ▷ b) ▷ c => c
    }

    // ---------- addAll ----------
    @Test
    @DisplayName("addAll：原地修改左集合並回傳左集合實例")
    void addAll_mutatesLeft_andReturnsSameInstance() {
        var addAll = ops.<Integer>addAll();

        List<Integer> left = new ArrayList<>(List.of(1, 2));
        List<Integer> right = new ArrayList<>(List.of(3, 4));

        Collection<Integer> result = addAll.apply(left, right);

        assertSame(left, result, "應回傳左集合本身");
        assertEquals(List.of(1, 2, 3, 4), new ArrayList<>(result));
        assertEquals(List.of(3, 4), right, "右集合不應被改動");
    }

    @Test
    @DisplayName("addAll：在 reduce 中逐一合併成單一集合（左折疊，原地累加）")
    void addAll_reduce_mergesAll() {
        var addAll = ops.<Integer>addAll();

        List<Integer> a = new ArrayList<>(List.of(1));
        List<Integer> b = new ArrayList<>(List.of(2, 3));
        List<Integer> c = new ArrayList<>(List.of(4));

        // 無 identity 的 reduce：會回傳第1個集合（經過原地累加）
        Collection<Integer> reduced = Stream.of(a, b, c).reduce(addAll).orElseThrow();
        assertSame(a, reduced);
        assertEquals(List.of(1, 2, 3, 4), new ArrayList<>(reduced));
    }

    // ---------- min ----------
    @Test
    @DisplayName("min：含邊界；l<r 回 l，l>r 回 r，相等時回右值（右偏）")
    void min_inclusive_rightBiasedOnEqual() {
        var min = ops.<Integer>min();
        assertEquals(3, min.apply(3, 5));
        assertEquals(5, min.apply(7, 5));

        Integer x = 200;            // 確保不是 Integer cache 範圍
        Integer y = 200;
        Integer got = min.apply(x, y);
        assertSame(y, got, "相等時應回傳右參數（右偏）");

        // 也測 String（Comparable）
        var minStr = ops.<String>min();
        assertEquals("a", minStr.apply("a", "c"));
        assertEquals("b", minStr.apply("b", "b")); // 相等回右值
    }

    // ---------- max ----------
    @Test
    @DisplayName("max：含邊界；l>r 回 l，l<r 回 r，相等時回右值（右偏）")
    void max_inclusive_rightBiasedOnEqual() {
        var max = ops.<Integer>max();
        assertEquals(9, max.apply(9, 2));
        assertEquals(6, max.apply(4, 6));

        String s1 = "k";
        String s2 = "k";
        String got = ops.<String>max().apply(s1, s2);
        assertSame(s2, got, "相等時應回傳右參數（右偏）");
    }

    // ---------- totalInteger ----------
    @Test
    @DisplayName("totalInteger：加總整數")
    void totalInteger_sums() {
        var sum = ops.totalInteger();
        assertEquals(0, sum.apply(0, 0));
        assertEquals(7, sum.apply(3, 4));

        int reduced = Stream.of(1, 2, 3, 4).reduce(sum).orElse(0);
        assertEquals(10, reduced);
    }

    // ---------- totalLong ----------
    @Test
    @DisplayName("totalLong：加總長整數（避免溢位）")
    void totalLong_sums() {
        var sum = ops.totalLong();
        assertEquals(10L, sum.apply(6L, 4L));

        long reduced = Stream.of(1_000_000_000L, 2_000_000_000L, 3_000_000_000L)
                .reduce(sum).orElse(0L);
        assertEquals(6_000_000_000L, reduced);
    }
}
