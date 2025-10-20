package org.dotspace.oofp.support.dsl.conditional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PartitionedCollectorConditionConsumerTest {

    @Mock
    private ConditionPredicate<String> predicate;

    private PartitionedCollectorConditionConsumer<String> consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new PartitionedCollectorConditionConsumer<>(predicate);
    }

    @Test
    void accept_WhenPredicateReturnsTrue_ShouldAddToSuccessList() {
        when(predicate.test("test")).thenReturn(true);

        consumer.accept("test");

        assertEquals(1, consumer.getSuccess().size());
        assertEquals("test", consumer.getSuccess().get(0));
        assertTrue(consumer.getFailure().isEmpty());
    }

    @Test
    void accept_WhenPredicateReturnsFalse_ShouldAddToFailureList() {
        when(predicate.test("test")).thenReturn(false);

        consumer.accept("test");

        assertEquals(1, consumer.getFailure().size());
        assertEquals("test", consumer.getFailure().get(0));
        assertTrue(consumer.getSuccess().isEmpty());
    }

    @Test
    void accept_WhenInputIsNull_ShouldAddToFailureList() {
        consumer.accept(null);

        assertEquals(1, consumer.getFailure().size());
        assertNull(consumer.getFailure().get(0));
        assertTrue(consumer.getSuccess().isEmpty());
        verify(predicate, never()).test(any());
    }

    @Test
    void accept_MultipleItems_ShouldPartitionCorrectly() {
        when(predicate.test("pass1")).thenReturn(true);
        when(predicate.test("pass2")).thenReturn(true);
        when(predicate.test("fail1")).thenReturn(false);
        when(predicate.test("fail2")).thenReturn(false);

        consumer.accept("pass1");
        consumer.accept("fail1");
        consumer.accept("pass2");
        consumer.accept("fail2");
        consumer.accept(null);

        assertEquals(2, consumer.getSuccess().size());
        assertEquals(3, consumer.getFailure().size());
        assertTrue(consumer.getSuccess().contains("pass1"));
        assertTrue(consumer.getSuccess().contains("pass2"));
        assertTrue(consumer.getFailure().contains("fail1"));
        assertTrue(consumer.getFailure().contains("fail2"));
        assertTrue(consumer.getFailure().contains(null));
    }

    @Test
    void constructor_WithNullPredicate_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> 
            new PartitionedCollectorConditionConsumer<>(null));
    }

    @Test
    void getSuccess_ShouldReturnMutableList() {
        when(predicate.test("test")).thenReturn(true);
        consumer.accept("test");

        assertDoesNotThrow(() -> consumer.getSuccess().clear());
    }

    @Test
    void getFailure_ShouldReturnMutableList() {
        when(predicate.test("test")).thenReturn(false);
        consumer.accept("test");

        assertDoesNotThrow(() -> consumer.getFailure().clear());
    }
}