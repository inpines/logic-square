package org.dotspace.oofp.utils.oofp.functional.monad;

import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.Sequence;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SequenceTest {

    @Test
    void testOfWithArray() {
        String[] array = {"a", "b", "c"};
        Sequence<String> sequence = Sequence.of(array);
        assertEquals(3, sequence.size());
        assertEquals(Arrays.asList("a", "b", "c"), sequence.unwrap().toList());
    }

    @Test
    void testOfWithEmptyArray() {
        String[] array = {};
        Sequence<String> sequence = Sequence.of(array);
        assertEquals(0, sequence.size());
    }

    @Test
    void testFromCollection() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Sequence<Integer> sequence = Sequence.from(list);
        assertEquals(3, sequence.size());
        assertEquals(list, sequence.unwrap().toList());
    }

    @Test
    void testFromEmptyCollection() {
        List<String> emptyList = Collections.emptyList();
        Sequence<String> sequence = Sequence.from(emptyList);
        assertEquals(0, sequence.size());
    }

    @Test
    void testFromMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        
        Sequence<Pair<String, Integer>> sequence = Sequence.from(map);
        assertEquals(2, sequence.size());
        
        Set<String> keys = sequence.unwrap()
                .map(Pair::getLeft)
                .collect(Collectors.toSet());
        assertTrue(keys.contains("one"));
        assertTrue(keys.contains("two"));
    }

    @Test
    void testFromEmptyMap() {
        Map<String, Integer> emptyMap = Collections.emptyMap();
        Sequence<Pair<String, Integer>> sequence = Sequence.from(emptyMap);
        assertEquals(0, sequence.size());
    }

    @Test
    void testEmpty() {
        Sequence<String> sequence = Sequence.empty();
        assertEquals(0, sequence.size());
        assertTrue(sequence.unwrap().toList().isEmpty());
    }

    @Test
    void testUnfold() {
        Function<Integer, Maybe<Pair<Integer, Integer>>> generator =
            n -> n > 5 ? Maybe.empty() : Maybe.given(Pair.of(n, n + 1));
        
        Sequence<Integer> sequence = Sequence.unfold(1, generator);
        assertEquals(5, sequence.size());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), sequence.unwrap().toList());
    }

    @Test
    void testUnfoldWithEmptyResult() {
        Function<Integer, Maybe<Pair<Integer, Integer>>> generator = n -> Maybe.empty();
        
        Sequence<Integer> sequence = Sequence.unfold(1, generator);
        assertEquals(0, sequence.size());
    }

    @Test
    void testMap() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 2, 3});
        Sequence<String> mapped = sequence.map(Object::toString);
        assertEquals(Arrays.asList("1", "2", "3"), mapped.unwrap().toList());
    }

    @Test
    void testMapOnEmpty() {
        Sequence<Integer> empty = Sequence.empty();
        Sequence<String> mapped = empty.map(Object::toString);
        assertEquals(0, mapped.size());
    }

    @Test
    void testFlatMap() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 2, 3});
        Sequence<Integer> flatMapped = sequence.flatMap(n -> 
            Sequence.of(new Integer[]{n, n * 10}));
        assertEquals(Arrays.asList(1, 10, 2, 20, 3, 30), flatMapped.unwrap().toList());
    }

    @Test
    void testFlatMapOnEmpty() {
        Sequence<Integer> empty = Sequence.empty();
        Sequence<Integer> flatMapped = empty.flatMap(n -> 
            Sequence.of(new Integer[]{n, n * 10}));
        assertEquals(0, flatMapped.size());
    }

    @Test
    void testFilter() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 2, 3, 4, 5});
        Sequence<Integer> filtered = sequence.filter(n -> n % 2 == 0);
        assertEquals(Arrays.asList(2, 4), filtered.unwrap().toList());
    }

    @Test
    void testFilterNoneMatch() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 3, 5});
        Sequence<Integer> filtered = sequence.filter(n -> n % 2 == 0);
        assertEquals(0, filtered.size());
    }

    @Test
    void testDistinct() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 2, 2, 3, 3, 3});
        Sequence<Integer> distinct = sequence.distinct();
        assertEquals(Arrays.asList(1, 2, 3), distinct.unwrap().toList());
    }

    @Test
    void testDistinctOnEmpty() {
        Sequence<Integer> empty = Sequence.empty();
        Sequence<Integer> distinct = empty.distinct();
        assertEquals(0, distinct.size());
    }

    @Test
    void testSorted() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{3, 1, 4, 1, 5});
        Sequence<Integer> sorted = sequence.sorted();
        assertEquals(Arrays.asList(1, 1, 3, 4, 5), sorted.unwrap().toList());
    }

    @Test
    void testSortedWithComparator() {
        Sequence<String> sequence = Sequence.of(new String[]{"apple", "pie", "a"});
        Sequence<String> sorted = sequence.sorted(Comparator.comparing(String::length));
        assertEquals(Arrays.asList("a", "pie", "apple"), sorted.unwrap().toList());
    }

    @Test
    void testPartitionBy() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 2, 3, 4, 5});
        Map<Boolean, List<Integer>> partitioned = sequence.partitionBy(n -> n % 2 == 0);
        
        assertEquals(Arrays.asList(2, 4), partitioned.get(true));
        assertEquals(Arrays.asList(1, 3, 5), partitioned.get(false));
    }

    @Test
    void testGroupBy() {
        Sequence<String> sequence = Sequence.of(new String[]{"apple", "apricot", "banana", "blueberry"});
        Map<Character, List<String>> grouped = sequence.groupBy(s -> s.charAt(0));
        
        assertEquals(Arrays.asList("apple", "apricot"), grouped.get('a'));
        assertEquals(Arrays.asList("banana", "blueberry"), grouped.get('b'));
    }

    @Test
    void testGroupByWithDownstream() {
        Sequence<String> sequence = Sequence.of(new String[]{"apple", "apricot", "banana"});
        Map<Character, Long> grouped = sequence.groupBy(
            s -> s.charAt(0), 
            Collectors.counting()
        );
        
        assertEquals(2L, grouped.get('a'));
        assertEquals(1L, grouped.get('b'));
    }

    @Test
    void testFold() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 2, 3, 4, 5});
        Integer sum = sequence.fold(0, Integer::sum);
        assertEquals(15, sum);
    }

    @Test
    void testFoldOnEmpty() {
        Sequence<Integer> empty = Sequence.empty();
        Integer sum = empty.fold(10, Integer::sum);
        assertEquals(10, sum);
    }

    @Test
    void testZip() {
        Sequence<Integer> seq1 = Sequence.of(new Integer[]{1, 2, 3});
        Sequence<String> seq2 = Sequence.of(new String[]{"a", "b", "c"});
        
        Sequence<String> zipped = seq1.zip(seq2, (i, s) -> i + s);
        assertEquals(Arrays.asList("1a", "2b", "3c"), zipped.unwrap().toList());
    }

    @Test
    void testZipWithDifferentSizes() {
        Sequence<Integer> seq1 = Sequence.of(new Integer[]{1, 2, 3, 4, 5});
        Sequence<String> seq2 = Sequence.of(new String[]{"a", "b"});
        
        Sequence<String> zipped = seq1.zip(seq2, (i, s) -> i + s);
        assertEquals(Arrays.asList("1a", "2b"), zipped.unwrap().toList());
    }

    @Test
    void testZipWithEmptySequences() {
        Sequence<Integer> empty1 = Sequence.empty();
        Sequence<String> empty2 = Sequence.empty();
        
        Sequence<String> zipped = empty1.zip(empty2, (i, s) -> i + s);
        assertEquals(0, zipped.size());
    }

    @Test
    void testToMap() {
        Sequence<String> sequence = Sequence.of(new String[]{"apple", "banana", "cherry"});
        Map<Character, Integer> map = sequence.toMap(
            s -> s.charAt(0),
            String::length
        );
        
        assertEquals(5, map.get('a'));
        assertEquals(6, map.get('b'));
        assertEquals(6, map.get('c'));
    }

    @Test
    void testUnwrap() {
        Sequence<String> sequence = Sequence.of(new String[]{"a", "b", "c"});
        List<String> list = sequence.unwrap().toList();
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    @Test
    void testStream() {
        Sequence<Integer> sequence = Sequence.of(new Integer[]{1, 2, 3});
        long count = sequence.stream().count();
        assertEquals(3, count);
    }

    @Test
    void testSize() {
        Sequence<String> sequence = Sequence.of(new String[]{"a", "b", "c"});
        assertEquals(3, sequence.size());
    }

    @Test
    void testSizeOnEmpty() {
        Sequence<String> empty = Sequence.empty();
        assertEquals(0, empty.size());
    }

    @Test
    void testChainedOperations() {
        Sequence<Integer> result = Sequence.of(new Integer[]{1, 2, 3, 4, 5, 6})
            .filter(n -> n % 2 == 0)
            .map(n -> n * 2)
            .sorted();
        
        assertEquals(Arrays.asList(4, 8, 12), result.unwrap().toList());
    }

    @Test
    void testComplexChainedOperations() {
        Map<String, Integer> map = new HashMap<>();
        map.put("apple", 5);
        map.put("banana", 6);
        map.put("cherry", 6);
        
        List<String> result = Sequence.from(map)
            .filter(pair -> pair.getValue() > 5)
            .map(Pair::getKey)
            .sorted()
            .unwrap()
            .toList();
        
        assertEquals(Arrays.asList("banana", "cherry"), result);
    }
}