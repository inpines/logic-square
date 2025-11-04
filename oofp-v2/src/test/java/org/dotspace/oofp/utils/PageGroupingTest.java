package org.dotspace.oofp.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class PageGroupingTest {

    @Test
    @DisplayName("Should group data by string classifier")
    void groupingBy_WithStringClassifier_ShouldGroupCorrectly() {
        // Given
        List<String> data = Arrays.asList("apple", "banana", "avocado", "blueberry", "apricot");
        Function<String, String> classifier = s -> s.substring(0, 1); // First letter

        // When
        PageGrouping<String> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getGroupingKeys().size());
        assertTrue(result.getGroupingKeys().contains("a"));
        assertTrue(result.getGroupingKeys().contains("b"));

        List<String> aGroup = result.of("a");
        assertEquals(3, aGroup.size());
        assertTrue(aGroup.contains("apple"));
        assertTrue(aGroup.contains("avocado"));
        assertTrue(aGroup.contains("apricot"));

        List<String> bGroup = result.of("b");
        assertEquals(2, bGroup.size());
        assertTrue(bGroup.contains("banana"));
        assertTrue(bGroup.contains("blueberry"));
    }

    @Test
    @DisplayName("Should handle empty list")
    void groupingBy_WithEmptyList_ShouldReturnEmptyGroups() {
        // Given
        List<String> data = Collections.emptyList();
        Function<String, String> classifier = s -> s.substring(0, 1);

        // When
        PageGrouping<String> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertTrue(result.getGroupingKeys().isEmpty());
    }

    @Test
    @DisplayName("Should handle null list")
    void groupingBy_WithNullList_ShouldReturnEmptyGroups() {
        // Given
        List<String> data = null;
        Function<String, String> classifier = s -> s.substring(0, 1);

        // When
        PageGrouping<String> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertTrue(result.getGroupingKeys().isEmpty());
    }

    @Test
    @DisplayName("Should group numbers by even/odd")
    void groupingBy_WithNumbers_ShouldGroupByEvenOdd() {
        // Given
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Function<Integer, String> classifier = n -> n % 2 == 0 ? "even" : "odd";

        // When
        PageGrouping<Integer> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getGroupingKeys().size());
        assertTrue(result.getGroupingKeys().contains("even"));
        assertTrue(result.getGroupingKeys().contains("odd"));

        assertEquals(5, result.of("even").size());
        assertEquals(5, result.of("odd").size());

        assertTrue(result.of("even").contains(2));
        assertTrue(result.of("even").contains(4));
        assertTrue(result.of("odd").contains(1));
        assertTrue(result.of("odd").contains(3));
    }

    @Test
    @DisplayName("Should return null for non-existent key")
    void of_WithNonExistentKey_ShouldReturnNull() {
        // Given
        List<String> data = Arrays.asList("apple", "banana");
        Function<String, String> classifier = s -> s.substring(0, 1);
        PageGrouping<String> pageGrouping = PageGrouping.groupingBy(data, classifier);

        // When
        List<String> result = pageGrouping.of("z");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle single item groups")
    void groupingBy_WithSingleItemGroups_ShouldWorkCorrectly() {
        // Given
        List<String> data = Arrays.asList("apple", "banana", "cherry");
        Function<String, String> classifier = s -> s.substring(0, 1);

        // When
        PageGrouping<String> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getGroupingKeys().size());
        assertEquals(1, result.of("a").size());
        assertEquals(1, result.of("b").size());
        assertEquals(1, result.of("c").size());
    }

    @Test
    @DisplayName("Should handle duplicate values in same group")
    void groupingBy_WithDuplicateValues_ShouldIncludeAllDuplicates() {
        // Given
        List<String> data = Arrays.asList("apple", "apricot", "apple", "banana", "apple");
        Function<String, String> classifier = s -> s.substring(0, 1);

        // When
        PageGrouping<String> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getGroupingKeys().size());
        assertEquals(4, result.of("a").size()); // 3 apples + 1 apricot
        assertEquals(1, result.of("b").size());

        // Count occurrences of "apple" in group "a"
        long appleCount = result.of("a").stream().filter("apple"::equals).count();
        assertEquals(3, appleCount);
    }

    @Test
    @DisplayName("Should return sorted grouping keys")
    void getGroupingKeys_WithComparator_ShouldReturnSortedKeys() {
        // Given
        List<String> data = Arrays.asList("zebra", "apple", "banana", "cherry");
        Function<String, String> classifier = s -> s.substring(0, 1);
        PageGrouping<String> pageGrouping = PageGrouping.groupingBy(data, classifier);

        // When
        List<String> sortedKeys = pageGrouping.getGroupingKeys(Comparator.naturalOrder());

        // Then
        assertNotNull(sortedKeys);
        assertEquals(4, sortedKeys.size());
        assertEquals(Arrays.asList("a", "b", "c", "z"), sortedKeys);
    }

    @Test
    @DisplayName("Should return reverse sorted grouping keys")
    void getGroupingKeys_WithReverseComparator_ShouldReturnReverseSortedKeys() {
        // Given
        List<String> data = Arrays.asList("zebra", "apple", "banana", "cherry");
        Function<String, String> classifier = s -> s.substring(0, 1);
        PageGrouping<String> pageGrouping = PageGrouping.groupingBy(data, classifier);

        // When
        List<String> sortedKeys = pageGrouping.getGroupingKeys(Comparator.reverseOrder());

        // Then
        assertNotNull(sortedKeys);
        assertEquals(4, sortedKeys.size());
        assertEquals(Arrays.asList("z", "c", "b", "a"), sortedKeys);
    }

    @Test
    @DisplayName("Should handle complex objects")
    void groupingBy_WithComplexObjects_ShouldGroupCorrectly() {
        // Given
        List<Person> data = Arrays.asList(
                new Person("Alice", 25),
                new Person("Bob", 30),
                new Person("Charlie", 25),
                new Person("David", 35)
        );
        Function<Person, String> classifier = person -> "Age" + person.age;

        // When
        PageGrouping<Person> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getGroupingKeys().size());
        assertTrue(result.getGroupingKeys().contains("Age25"));
        assertTrue(result.getGroupingKeys().contains("Age30"));
        assertTrue(result.getGroupingKeys().contains("Age35"));

        assertEquals(2, result.of("Age25").size());
        assertEquals(1, result.of("Age30").size());
        assertEquals(1, result.of("Age35").size());
    }

    @Test
    @DisplayName("Should handle null values in data")
    void groupingBy_WithNullValues_ShouldHandleGracefully() {
        // Given
        List<String> data = Arrays.asList("apple", null, "banana", null);
        Function<String, String> classifier = s -> s == null ? "null" : s.substring(0, 1);

        // When
        PageGrouping<String> result = PageGrouping.groupingBy(data, classifier);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getGroupingKeys().size());
        assertTrue(result.getGroupingKeys().contains("a"));
        assertTrue(result.getGroupingKeys().contains("b"));
        assertTrue(result.getGroupingKeys().contains("null"));

        assertEquals(2, result.of("null").size());
        assertEquals(1, result.of("a").size());
        assertEquals(1, result.of("b").size());
    }

    // Helper class for testing
    private static class Person {
        final String name;
        final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }
}
