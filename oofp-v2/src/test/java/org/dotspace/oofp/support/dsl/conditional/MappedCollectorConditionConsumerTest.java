package org.dotspace.oofp.support.dsl.conditional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MappedCollectorConditionConsumerTest {

    @Mock
    private ConditionPredicate<String> mockPredicate;

    @Mock
    private Function<String, Integer> mockMapper;

    private MappedCollectorConditionConsumer<String, Integer> collector;

    @BeforeEach
    void setUp() {
        collector = new MappedCollectorConditionConsumer<>(mockPredicate, mockMapper);
    }

    @Test
    void constructor_WithValidParameters_CreatesInstance() {
        // Assert
        assertNotNull(collector);
        assertNotNull(collector.getCollected());
        assertTrue(collector.getCollected().isEmpty());
    }

    @Test
    void constructor_WithNullPredicate_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            new MappedCollectorConditionConsumer<>(null, mockMapper));
    }

    @Test
    void constructor_WithNullMapper_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            new MappedCollectorConditionConsumer<String, Integer>(mockPredicate, null));
    }

    @Test
    void accept_WhenPredicateReturnsTrue_MapsThenAddsItemToCollection() {
        // Arrange
        String testItem = "test";
        Integer mappedValue = 42;
        when(mockPredicate.test(testItem)).thenReturn(true);
        when(mockMapper.apply(testItem)).thenReturn(mappedValue);

        // Act
        collector.accept(testItem);

        // Assert
        List<Integer> collected = collector.getCollected();
        assertEquals(1, collected.size());
        assertEquals(mappedValue, collected.get(0));
        verify(mockPredicate).test(testItem);
        verify(mockMapper).apply(testItem);
    }

    @Test
    void accept_WhenPredicateReturnsFalse_DoesNotMapOrAddItemToCollection() {
        // Arrange
        String testItem = "test";
        when(mockPredicate.test(testItem)).thenReturn(false);

        // Act
        collector.accept(testItem);

        // Assert
        List<Integer> collected = collector.getCollected();
        assertTrue(collected.isEmpty());
        verify(mockPredicate).test(testItem);
        verify(mockMapper, never()).apply(testItem);
    }

    @Test
    void accept_WithMultipleItems_CollectsOnlyMappedMatchingItems() {
        // Arrange
        String item1 = "match1";
        String item2 = "no-match";
        String item3 = "match2";
        Integer mapped1 = 10;
        Integer mapped3 = 30;
        
        when(mockPredicate.test(item1)).thenReturn(true);
        when(mockPredicate.test(item2)).thenReturn(false);
        when(mockPredicate.test(item3)).thenReturn(true);
        when(mockMapper.apply(item1)).thenReturn(mapped1);
        when(mockMapper.apply(item3)).thenReturn(mapped3);

        // Act
        collector.accept(item1);
        collector.accept(item2);
        collector.accept(item3);

        // Assert
        List<Integer> collected = collector.getCollected();
        assertEquals(2, collected.size());
        assertEquals(mapped1, collected.get(0));
        assertEquals(mapped3, collected.get(1));
        verify(mockMapper, never()).apply(item2);
    }

    @Test
    void accept_WithNullItem_HandlesGracefully() {
        // Act
        collector.accept(null);

        // Assert
        List<Integer> collected = collector.getCollected();
        assertEquals(0, collected.size());
        verify(mockPredicate, never()).test(null);
        verify(mockMapper, never()).apply(null);
    }

    @Test
    void accept_MultipleCallsWithSameItem_CollectsMappedValueMultipleTimes() {
        // Arrange
        String testItem = "duplicate";
        Integer mappedValue = 99;
        when(mockPredicate.test(testItem)).thenReturn(true);
        when(mockMapper.apply(testItem)).thenReturn(mappedValue);

        // Act
        collector.accept(testItem);
        collector.accept(testItem);
        collector.accept(testItem);

        // Assert
        List<Integer> collected = collector.getCollected();
        assertEquals(3, collected.size());
        assertTrue(collected.stream().allMatch(item -> item.equals(mappedValue)));
        verify(mockPredicate, times(3)).test(testItem);
        verify(mockMapper, times(3)).apply(testItem);
    }

    @Test
    void getCollected_ReturnsModifiableList() {
        // Arrange
        String testItem = "test";
        Integer mappedValue = 123;
        when(mockPredicate.test(testItem)).thenReturn(true);
        when(mockMapper.apply(testItem)).thenReturn(mappedValue);
        collector.accept(testItem);

        // Act
        List<Integer> collected = collector.getCollected();
        collected.add(456);

        // Assert
        assertEquals(2, collected.size());
        assertEquals(mappedValue, collected.get(0));
        assertEquals(Integer.valueOf(456), collected.get(1));
    }

    @Test
    void implementsConditionConsumer_Interface() {
        // Assert
        assertInstanceOf(ConditionConsumer.class, collector);
    }

    @Test
    void accept_WithDifferentMappingResults_CollectsAllMappedValues() {
        // Arrange
        String item1 = "one";
        String item2 = "two";
        Integer mapped1 = 1;
        Integer mapped2 = 2;
        
        when(mockPredicate.test(item1)).thenReturn(true);
        when(mockPredicate.test(item2)).thenReturn(true);
        when(mockMapper.apply(item1)).thenReturn(mapped1);
        when(mockMapper.apply(item2)).thenReturn(mapped2);

        // Act
        collector.accept(item1);
        collector.accept(item2);

        // Assert
        List<Integer> collected = collector.getCollected();
        assertEquals(2, collected.size());
        assertEquals(mapped1, collected.get(0));
        assertEquals(mapped2, collected.get(1));
    }
}