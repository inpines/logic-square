package org.dotspace.oofp.utils;

import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PaginationTest {

    @BeforeEach
    void setUp() {
        // Clear pagination IDs before each test to ensure clean state
        Pagination.paginationIds.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        Pagination.paginationIds.clear();
    }

    @Test
    @DisplayName("paginate creates Pagination instance with unique ID")
    void paginate_WithValidData_ShouldCreatePaginationWithUniqueId() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        int limit = 2;

        // When
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // Then
        assertNotNull(pagination);
        assertNotNull(pagination.getPaginationId());
        assertTrue(Pagination.paginationIds.contains(pagination.getPaginationId()));
    }

    @Test
    @DisplayName("paginate with null data should handle gracefully")
    void paginate_WithNullData_ShouldHandleGracefully() {
        // Given
        int limit = 2;

        // When
        Pagination<String> pagination = Pagination.paginate(null, limit);

        // Then
        assertNotNull(pagination);
        assertNotNull(pagination.getPaginationId());
    }

    @Test
    @DisplayName("paginate with empty data should work")
    void paginate_WithEmptyData_ShouldWork() {
        // Given
        List<String> data = Collections.emptyList();
        int limit = 2;

        // When
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // Then
        assertNotNull(pagination);
        assertNotNull(pagination.getPaginationId());
        assertTrue(pagination.of(1).isEmpty());
    }

    @Test
    @DisplayName("paginate with zero limit should work")
    void paginate_WithZeroLimit_ShouldWork() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        int limit = 0;

        // When
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // Then
        assertNotNull(pagination);
        assertTrue(pagination.of(1).isEmpty());
    }

    @Test
    @DisplayName("paginate with negative limit should throw IllegalArgumentException")
    void paginate_WithNegativeLimit_ShouldThrowException() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        int limit = -5;

        // When
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // Then
        assertNotNull(pagination);
        assertNotNull(pagination.getPaginationId());
        assertThrows(IllegalArgumentException.class, () -> pagination.of(1));
    }

    @Test
    @DisplayName("of method returns correct page data")
    void of_WithValidPageNumber_ShouldReturnCorrectData() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        int limit = 2;
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // When
        List<String> page1 = pagination.of(1);
        List<String> page2 = pagination.of(2);
        List<String> page3 = pagination.of(3);

        // Then
        assertEquals(Arrays.asList("item1", "item2"), page1);
        assertEquals(Arrays.asList("item3", "item4"), page2);
        assertEquals(List.of("item5"), page3);
    }

    @Test
    @DisplayName("of method with page number zero should throw IllegalArgumentException")
    void of_WithPageNumberZero_ShouldThrowException() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        int limit = 2;
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pagination.of(0));
    }

    @Test
    @DisplayName("of method with negative page number should throw IllegalArgumentException")
    void of_WithNegativePageNumber_ShouldThrowException() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        int limit = 2;
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pagination.of(-1));
    }

    @Test
    @DisplayName("of method with page number beyond total pages should return empty list")
    void of_WithPageNumberBeyondTotalPages_ShouldReturnEmptyList() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        int limit = 2;
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // When
        List<String> result = pagination.of(10);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("of method with null data should handle gracefully")
    void of_WithNullData_ShouldHandleGracefully() {
        // Given
        int limit = 2;
        Pagination<String> pagination = Pagination.paginate(null, limit);

        // When & Then
        assertThrows(NullPointerException.class, () -> pagination.of(1));
    }

    @Test
    @DisplayName("getTotalPage with zero limit should return empty")
    void getTotalPage_WithZeroLimit_ShouldReturnEmpty() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        Pagination<String> pagination = Pagination.paginate(data, 0);

        // When & Then
        List<String> result = pagination.of(1);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Pagination should work with different data types")
    void pagination_WithDifferentDataTypes_ShouldWork() {
        // Test with Integer
        List<Integer> intData = Arrays.asList(1, 2, 3, 4, 5);
        Pagination<Integer> intPagination = Pagination.paginate(intData, 2);
        assertEquals(Arrays.asList(1, 2), intPagination.of(1));
        assertEquals(Arrays.asList(3, 4), intPagination.of(2));
        assertEquals(List.of(5), intPagination.of(3));

        // Test with custom objects
        List<TestObject> objData = Arrays.asList(
                new TestObject("obj1"),
                new TestObject("obj2"),
                new TestObject("obj3")
        );
        Pagination<TestObject> objPagination = Pagination.paginate(objData, 2);
        List<TestObject> firstPage = objPagination.of(1);
        assertEquals(2, firstPage.size());
        assertEquals("obj1", firstPage.get(0).getName());
        assertEquals("obj2", firstPage.get(1).getName());
    }

    @Test
    @DisplayName("Pagination IDs should be unique across multiple instances")
    void pagination_ShouldGenerateUniqueIds() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        Set<String> generatedIds = new HashSet<>();

        // When
        for (int i = 0; i < 100; i++) {
            Pagination<String> pagination = Pagination.paginate(data, 2);
            String id = pagination.getPaginationId();
            assertFalse(generatedIds.contains(id), "Duplicate pagination ID found: " + id);
            generatedIds.add(id);
        }

        // Then
        assertEquals(100, generatedIds.size());
        assertEquals(100, Pagination.paginationIds.size());
    }

    @Test
    @DisplayName("Pagination should be thread-safe for ID generation")
    void pagination_ShouldBeThreadSafeForIdGeneration() throws InterruptedException {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<String> generatedIds = Collections.synchronizedSet(new HashSet<>());

        // When
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Pagination<String> pagination = Pagination.paginate(data, 2);
                    generatedIds.add(pagination.getPaginationId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertEquals(threadCount, generatedIds.size(), "All pagination IDs should be unique");
        assertEquals(threadCount, Pagination.paginationIds.size());
    }

    @Test
    @DisplayName("Pagination should handle data with null elements")
    void pagination_WithNullElements_ShouldHandleGracefully() {
        // Given
        List<String> data = Arrays.asList("item1", null, "item3", null, "item5");
        int limit = 2;
        Pagination<String> pagination = Pagination.paginate(data, limit);

        // When
        List<String> page1 = pagination.of(1);
        List<String> page2 = pagination.of(2);
        List<String> page3 = pagination.of(3);

        // Then
        assertEquals(Arrays.asList("item1", null), page1);
        assertEquals(Arrays.asList("item3", null), page2);
        assertEquals(List.of("item5"), page3);
    }

    @Test
    @DisplayName("Pagination should maintain immutability of original data")
    void pagination_ShouldNotModifyOriginalData() {
        // Given
        List<String> originalData = new ArrayList<>(Arrays.asList("item1", "item2", "item3", "item4"));
        List<String> dataCopy = new ArrayList<>(originalData);
        Pagination<String> pagination = Pagination.paginate(originalData, 2);

        // When
        List<String> page1 = pagination.of(1);

        // Then
        assertThrows(UnsupportedOperationException.class, page1::clear);
        assertEquals(dataCopy, originalData); // Original data unchanged
        assertEquals(Arrays.asList("item1", "item2"), pagination.of(1)); // Pagination still works correctly
    }

    // Helper class for testing with custom objects
    @Getter
    private static class TestObject {
        private final String name;

        public TestObject(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
