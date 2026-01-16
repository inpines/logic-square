package org.dotspace.oofp.support.dsl.conditional;

import org.dotspace.oofp.utils.dsl.conditional.conditional.CollectorConditionConsumer;
import org.dotspace.oofp.utils.dsl.conditional.conditional.ConditionConsumer;
import org.dotspace.oofp.utils.dsl.conditional.conditional.ConditionPredicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectorConditionConsumerTest {

    @Mock
    private ConditionPredicate<String> mockPredicate;

    private CollectorConditionConsumer<String> collector;

    @BeforeEach
    void setUp() {
        collector = new CollectorConditionConsumer<>(mockPredicate);
    }

    @Test
    void constructor_WithValidPredicate_CreatesInstance() {
        // Assert
        assertNotNull(collector);
        assertNotNull(collector.getCollected());
        assertTrue(collector.getCollected().isEmpty());
    }

    @Test
    void constructor_WithNullPredicate_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> 
            new CollectorConditionConsumer<String>(null));
    }

    @Test
    void accept_WhenPredicateReturnsTrue_AddsItemToCollection() {
        // Arrange
        String testItem = "test";
        when(mockPredicate.test(testItem)).thenReturn(true);

        // Act
        collector.accept(testItem);

        // Assert
        List<String> collected = collector.getCollected();
        assertEquals(1, collected.size());
        assertEquals(testItem, collected.get(0));
        verify(mockPredicate).test(testItem);
    }

    @Test
    void accept_WhenPredicateReturnsFalse_DoesNotAddItemToCollection() {
        // Arrange
        String testItem = "test";
        when(mockPredicate.test(testItem)).thenReturn(false);

        // Act
        collector.accept(testItem);

        // Assert
        List<String> collected = collector.getCollected();
        assertTrue(collected.isEmpty());
        verify(mockPredicate).test(testItem);
    }

    @Test
    void accept_WithMultipleItems_CollectsOnlyMatchingItems() {
        // Arrange
        String item1 = "match";
        String item2 = "no-match";
        String item3 = "another-match";
        
        when(mockPredicate.test(item1)).thenReturn(true);
        when(mockPredicate.test(item2)).thenReturn(false);
        when(mockPredicate.test(item3)).thenReturn(true);

        // Act
        collector.accept(item1);
        collector.accept(item2);
        collector.accept(item3);

        // Assert
        List<String> collected = collector.getCollected();
        assertEquals(2, collected.size());
        assertEquals(item1, collected.get(0));
        assertEquals(item3, collected.get(1));
    }

    @Test
    void accept_WithNullItem_HandlesGracefully() {
        // Arrange

        // Act
        collector.accept(null);

        // Assert
        List<String> collected = collector.getCollected();
        assertEquals(0, collected.size());
        verify(mockPredicate, never()).test(null);
    }

    @Test
    void getCollected_ReturnsModifiableList() {
        // Arrange
        String testItem = "test";
        when(mockPredicate.test(testItem)).thenReturn(true);
        collector.accept(testItem);

        // Act
        List<String> collected = collector.getCollected();
        collected.add("manually-added");

        // Assert
        assertEquals(2, collected.size());
        assertEquals("manually-added", collected.get(1));
    }

    @Test
    void accept_MultipleCallsWithSameItem_CollectsMultipleTimes() {
        // Arrange
        String testItem = "duplicate";
        when(mockPredicate.test(testItem)).thenReturn(true);

        // Act
        collector.accept(testItem);
        collector.accept(testItem);
        collector.accept(testItem);

        // Assert
        List<String> collected = collector.getCollected();
        assertEquals(3, collected.size());
        assertTrue(collected.stream().allMatch(item -> item.equals(testItem)));
    }

    @Test
    void implementsConditionConsumer_Interface() {
        // Assert
        assertInstanceOf(ConditionConsumer.class, collector);
    }
}