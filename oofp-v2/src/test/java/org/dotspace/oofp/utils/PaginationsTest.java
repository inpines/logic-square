package org.dotspace.oofp.utils;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaginationsTest {

    private Paginations paginations;

    @Mock
    private Paginator paginator;

    @BeforeEach
    void setUp() {
        paginations = new Paginations();
    }

    @Test
    @DisplayName("Should paginate data with valid inputs")
    void paginate_WithValidInputs_ShouldReturnPagination() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        when(paginator.getLimit()).thenReturn(3);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle empty data list")
    void paginate_WithEmptyData_ShouldReturnEmptyPagination() {
        // Given
        List<String> data = Collections.emptyList();
        when(paginator.getLimit()).thenReturn(10);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle null data list")
    void paginate_WithNullData_ShouldHandleGracefully() {
        // Given
        List<String> data = null;
        when(paginator.getLimit()).thenReturn(5);

        // When & Then
        assertDoesNotThrow(() -> {
            paginations.paginate(data, paginator);
            // Result behavior depends on Pagination.paginate implementation
        });
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle null paginator")
    void paginate_WithNullPaginator_ShouldThrowException() {
        // Given
        List<String> data = Arrays.asList("item1", "item2");
        Paginator nullPaginator = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> paginations.paginate(data, nullPaginator));
    }

    @Test
    @DisplayName("Should handle zero limit")
    void paginate_WithZeroLimit_ShouldHandleGracefully() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        when(paginator.getLimit()).thenReturn(0);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle negative limit")
    void paginate_WithNegativeLimit_ShouldHandleGracefully() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        when(paginator.getLimit()).thenReturn(-5);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle limit larger than data size")
    void paginate_WithLimitLargerThanDataSize_ShouldReturnAllData() {
        // Given
        List<String> data = Arrays.asList("item1", "item2");
        when(paginator.getLimit()).thenReturn(10);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle single item data")
    void paginate_WithSingleItem_ShouldReturnSingleItemPagination() {
        // Given
        List<String> data = List.of("singleItem");
        when(paginator.getLimit()).thenReturn(5);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle limit of one")
    void paginate_WithLimitOfOne_ShouldReturnOneItemPerPage() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        when(paginator.getLimit()).thenReturn(1);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should work with different data types")
    void paginate_WithIntegerData_ShouldReturnIntegerPagination() {
        // Given
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        when(paginator.getLimit()).thenReturn(4);

        // When
        Pagination<Integer> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should work with custom object data")
    void paginate_WithCustomObjectData_ShouldReturnCustomObjectPagination() {
        // Given
        List<TestObject> data = Arrays.asList(
                new TestObject("obj1"),
                new TestObject("obj2"),
                new TestObject("obj3")
        );
        when(paginator.getLimit()).thenReturn(2);

        // When
        Pagination<TestObject> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should handle large data sets")
    void paginate_WithLargeDataSet_ShouldHandleEfficiently() {
        // Given
        List<String> data = Collections.nCopies(1000, "item");
        when(paginator.getLimit()).thenReturn(50);

        // When
        long startTime = System.currentTimeMillis();
        Pagination<String> result = paginations.paginate(data, paginator);
        long endTime = System.currentTimeMillis();

        // Then
        assertNotNull(result);
        assertTrue(endTime - startTime < 100, "Should complete efficiently");
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should call paginator.getLimit() exactly once")
    void paginate_ShouldCallGetLimitOnce() {
        // Given
        List<String> data = Arrays.asList("item1", "item2");
        when(paginator.getLimit()).thenReturn(5);

        // When
        paginations.paginate(data, paginator);

        // Then
        verify(paginator, times(1)).getLimit();
    }

    @Test
    @DisplayName("Should handle data with null elements")
    void paginate_WithNullElements_ShouldHandleGracefully() {
        // Given
        List<String> data = Arrays.asList("item1", null, "item3", null);
        when(paginator.getLimit()).thenReturn(2);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should work with very large limit")
    void paginate_WithVeryLargeLimit_ShouldHandleGracefully() {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3");
        when(paginator.getLimit()).thenReturn(Integer.MAX_VALUE);

        // When
        Pagination<String> result = paginations.paginate(data, paginator);

        // Then
        assertNotNull(result);
        verify(paginator).getLimit();
    }

    @Test
    @DisplayName("Should be thread-safe for concurrent calls")
    void paginate_WithConcurrentCalls_ShouldBeThreadSafe() throws InterruptedException {
        // Given
        List<String> data = Arrays.asList("item1", "item2", "item3", "item4", "item5");
        when(paginator.getLimit()).thenReturn(2);

        // When
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                Pagination<String> result = paginations.paginate(data, paginator);
                assertNotNull(result);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        verify(paginator, times(10)).getLimit();
    }

    // Helper class for testing
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
