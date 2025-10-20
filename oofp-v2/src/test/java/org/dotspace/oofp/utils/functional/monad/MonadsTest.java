package org.dotspace.oofp.utils.functional.monad;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class MonadsTest {

    @Test
    void testMaybeWithNullValue() {
        Maybe<String> result = Monads.maybe(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testMaybeWithNonNullValue() {
        Maybe<String> result = Monads.maybe("test");
        assertTrue(result.isPresent());
        assertEquals("test", result.orElse("default"));
    }

    @Test
    void testOfWithNonNullValue() {
        Maybe<String> result = Monads.of("test");
        assertTrue(result.isPresent());
        assertEquals("test", result.orElse("default"));
    }

    @Test
    void testOfWithNullValueThrowsException() {
        assertThrows(NullPointerException.class, () -> Monads.of(null));
    }

    @Test
    void testSequenceFromCollection() {
        List<String> list = Arrays.asList("a", "b", "c");
        Sequence<String> result = Monads.sequence(list);
        assertEquals(3, result.size());
    }

    @Test
    void testSequenceFromEmptyCollection() {
        List<String> emptyList = Collections.emptyList();
        Sequence<String> result = Monads.sequence(emptyList);
        assertEquals(0, result.size());
    }

    @Test
    void testSequenceFromMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        
        Sequence<Pair<String, Integer>> result = Monads.sequence(map);
        assertEquals(2, result.size());
    }

    @Test
    void testSequenceFromArray() {
        String[] array = {"x", "y", "z"};
        Sequence<String> result = Monads.sequence(array);
        assertEquals(3, result.size());
    }

    @Test
    void testUnfold() {
        Function<Integer, Optional<Pair<Integer, Integer>>> generator = 
            n -> n > 5 ? Optional.empty() : Optional.of(Pair.of(n, n + 1));
        
        Sequence<Integer> result = Monads.unfold(1, generator);
        assertEquals(5, result.size());
    }

    @Test
    void testTaskFromSupplier() {
        Task<String> result = Monads.task(() -> "computed");
        assertNotNull(result);
    }

    @Test
    void testFutureFromCompletableFuture() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("async result");
        Task<String> result = Monads.future(future);
        assertNotNull(result);
    }
}