package org.dotspace.oofp.support.msg;

import org.dotspace.oofp.support.expression.ExpressionEvaluation;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageSupportTest {

    @Mock
    private ExpressionEvaluations expressionEvaluations;

    @Mock
    private ExpressionEvaluation expressionEvaluation;

    private MessageSupport messageSupport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        messageSupport = new MessageSupport(expressionEvaluations);
    }

    @Test
    void testGetMessageUsingProperties_withValidExpression() {
        // Given
        String msgFormat = "Hello ${name}!";
        TestModel model = new TestModel("John");
        
        when(expressionEvaluations.evaluate("name")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(model)).thenReturn("John");

        // When
        String result = messageSupport.getMessageUsingProperties(msgFormat, model);

        // Then
        assertEquals("Hello John!", result);
    }

    @Test
    void testGetMessageUsingProperties_withDefaultValue() {
        // Given
        String msgFormat = "Hello ${name:Guest}!";
        TestModel model = new TestModel(null);
        
        when(expressionEvaluations.evaluate("name")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(model)).thenReturn(null);

        // When
        String result = messageSupport.getMessageUsingProperties(msgFormat, model);

        // Then
        assertEquals("Hello Guest!", result);
    }

    @Test
    void testGetMessageUsingProperties_withNullExpression() {
        // Given
        String msgFormat = "Hello ${invalidProp}!";
        TestModel model = new TestModel("John");
        
        when(expressionEvaluations.evaluate("invalidProp")).thenReturn(null);

        // When
        String result = messageSupport.getMessageUsingProperties(msgFormat, model);

        // Then
        assertEquals("Hello <null>!", result);
    }

    @Test
    void testGetMessageUsingProperties_withMultipleExpressions() {
        // Given
        String msgFormat = "${greeting} ${name}!";
        TestModel model = new TestModel("John");
        
        when(expressionEvaluations.evaluate("greeting")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(model)).thenReturn("Hello");

        ExpressionEvaluation nameExpression = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("name")).thenReturn(nameExpression);
        when(nameExpression.getValue(model)).thenReturn("John");

        // When
        String result = messageSupport.getMessageUsingProperties(msgFormat, model);

        // Then
        assertEquals("Hello John!", result);
    }

    @Test
    void testGetMessageUsingPropertiesWithParameters() {
        // Given
        String msgFormat = "Hello ${name}! Your score is #{score:0}.";
        TestModel model = new TestModel("John");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("score", 95);
        
        when(expressionEvaluations.evaluate("name")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(model)).thenReturn("John");

        // When
        String result = messageSupport.getMessageUsingProperties(msgFormat, model, parameters);

        // Then
        assertEquals("Hello John! Your score is 95.", result);
    }

    @Test
    void testGetMessageUsingPropertiesWithParameters_withDefaultParameter() {
        // Given
        String msgFormat = "Hello ${name}! Your score is #{score:0}.";
        TestModel model = new TestModel("John");
        Map<String, Object> parameters = new HashMap<>();
        
        when(expressionEvaluations.evaluate("name")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(model)).thenReturn("John");

        // When
        String result = messageSupport.getMessageUsingProperties(msgFormat, model, parameters);

        // Then
        assertEquals("Hello John! Your score is 0.", result);
    }

    private static Stream<Arguments> obfuscatedOptionsProvider() {
        return Stream.of(
                Arguments.of("1234567890", "*", "12*****890"),
                Arguments.of("A1234567890", "*", "A1*****7890"),
                Arguments.of("AB", "*", "**"),
                Arguments.of("A", "*", "*"),
                Arguments.of(null, "*", null),
                Arguments.of("", "*", ""),
                Arguments.of("test", null, "test")
        );
    }
    @ParameterizedTest
    @MethodSource("obfuscatedOptionsProvider")
    void testGetObfuscatedString_withContitional(String input, String mask, String expect) {
        // Given

        // Action
        String result = messageSupport.getObfuscatedString(input, mask);

        // Then
        assertEquals(expect, result);

    }

    @Test
    void testGetMessageUsingProperties_throwsExceptionForNullFormat() {
        // Given
        TestModel model = new TestModel("John");

        // When & Then
        assertThrows(NullPointerException.class, () -> 
            messageSupport.getMessageUsingProperties(null, model));
    }

    private record TestModel(String name) {
    }
}