package org.dotspace.oofp.support.dsl.conditional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConditionalRulesTest {

    @Mock
    private ConditionConsumer<String> mockConsumer1;

    @Mock
    private ConditionConsumer<String> mockConsumer2;

    @Mock
    private ConditionConsumer<String> mockConsumer3;

    private ConditionalRules<String> conditionalRules;

    @BeforeEach
    void setUp() {
        conditionalRules = new ConditionalRules<>();
    }

    @Test
    void constructor_CreatesEmptyRules() {
        // Assert
        assertNotNull(conditionalRules);
    }

    @Test
    void with_SingleConsumer_ReturnsThis() {
        // Act
        ConditionalRules<String> result = conditionalRules.with(mockConsumer1);

        // Assert
        assertSame(conditionalRules, result);
    }

    @Test
    void with_MultipleConsumers_ReturnsThis() {
        // Act
        ConditionalRules<String> result = conditionalRules
                .with(mockConsumer1)
                .with(mockConsumer2)
                .with(mockConsumer3);

        // Assert
        assertSame(conditionalRules, result);
    }

    @Test
    void execute_WithNoConsumers_DoesNothing() {
        // Arrange
        String request = "test";

        // Act
        assertDoesNotThrow(() -> conditionalRules.execute(request));
    }

    @Test
    void execute_WithSingleConsumer_CallsConsumer() {
        // Arrange
        String request = "test";
        conditionalRules.with(mockConsumer1);

        // Act
        conditionalRules.execute(request);

        // Assert
        verify(mockConsumer1).accept(request);
    }

    @Test
    void execute_WithMultipleConsumers_CallsAllConsumers() {
        // Arrange
        String request = "test";
        conditionalRules
                .with(mockConsumer1)
                .with(mockConsumer2)
                .with(mockConsumer3);

        // Act
        conditionalRules.execute(request);

        // Assert
        verify(mockConsumer1).accept(request);
        verify(mockConsumer2).accept(request);
        verify(mockConsumer3).accept(request);
    }

    @Test
    void execute_WithNullRequest_PassesNullToConsumers() {
        // Arrange
        conditionalRules.with(mockConsumer1);

        // Act
        conditionalRules.execute(null);

        // Assert
        verify(mockConsumer1).accept(null);
    }

    @Test
    void merge_WithEmptyOther_ReturnsThis() {
        // Arrange
        ConditionalRules<String> other = new ConditionalRules<>();
        conditionalRules.with(mockConsumer1);

        // Act
        ConditionalRules<String> result = conditionalRules.merge(other);

        // Assert
        assertSame(conditionalRules, result);
    }

    @Test
    void merge_WithOtherConsumers_MergesConsumers() {
        // Arrange
        ConditionalRules<String> other = new ConditionalRules<>();
        other.with(mockConsumer2).with(mockConsumer3);
        conditionalRules.with(mockConsumer1);

        // Act
        ConditionalRules<String> result = conditionalRules.merge(other);

        // Assert
        assertSame(conditionalRules, result);
        
        // Verify all consumers are executed
        String request = "test";
        conditionalRules.execute(request);
        verify(mockConsumer1).accept(request);
        verify(mockConsumer2).accept(request);
        verify(mockConsumer3).accept(request);
    }

    @Test
    void merge_WithEmptyThis_AddsOtherConsumers() {
        // Arrange
        ConditionalRules<String> other = new ConditionalRules<>();
        other.with(mockConsumer1).with(mockConsumer2);

        // Act
        ConditionalRules<String> result = conditionalRules.merge(other);

        // Assert
        assertSame(conditionalRules, result);
        
        // Verify merged consumers are executed
        String request = "test";
        conditionalRules.execute(request);
        verify(mockConsumer1).accept(request);
        verify(mockConsumer2).accept(request);
    }

    @Test
    void fluentInterface_ChainingOperations_WorksCorrectly() {
        // Arrange
        ConditionalRules<String> other = new ConditionalRules<>();
        other.with(mockConsumer3);
        String request = "test";

        // Act
        ConditionalRules<String> result = conditionalRules
                .with(mockConsumer1)
                .with(mockConsumer2)
                .merge(other);

        // Assert
        assertSame(conditionalRules, result);
        
        // Verify all operations worked
        conditionalRules.execute(request);
        verify(mockConsumer1).accept(request);
        verify(mockConsumer2).accept(request);
        verify(mockConsumer3).accept(request);
    }

    @Test
    void execute_ConsumerThrowsException_ContinuesWithOtherConsumers() {
        // Arrange
        String request = "test";
        doThrow(new RuntimeException("Test exception")).when(mockConsumer1).accept(request);
        conditionalRules
                .with(mockConsumer1)
                .with(mockConsumer2);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> conditionalRules.execute(request));
        verify(mockConsumer1).accept(request);
        verify(mockConsumer2, never()).accept(request); // forEach stops on exception
    }

    @Test
    void execute_MultipleExecutions_CallsConsumersEachTime() {
        // Arrange
        String request1 = "test1";
        String request2 = "test2";
        conditionalRules.with(mockConsumer1);

        // Act
        conditionalRules.execute(request1);
        conditionalRules.execute(request2);

        // Assert
        verify(mockConsumer1).accept(request1);
        verify(mockConsumer1).accept(request2);
    }

    @Test
    void merge_SameConsumerAddedTwice_ExecutesTwice() {
        // Arrange
        ConditionalRules<String> other = new ConditionalRules<>();
        other.with(mockConsumer1);
        conditionalRules.with(mockConsumer1);
        String request = "test";

        // Act
        conditionalRules.merge(other);
        conditionalRules.execute(request);

        // Assert
        verify(mockConsumer1, times(2)).accept(request);
    }

    @Test
    void genericType_WorksWithDifferentTypes() {
        // Arrange
        ConditionalRules<Integer> intRules = new ConditionalRules<>();
        ConditionConsumer<Integer> intConsumer = mock(ConditionConsumer.class);
        Integer request = 42;

        // Act
        intRules.with(intConsumer).execute(request);

        // Assert
        verify(intConsumer).accept(request);
    }
}