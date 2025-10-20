package org.dotspace.oofp.support.expression;

import lombok.Getter;
import lombok.Setter;
import org.dotspace.oofp.utils.dsl.StepContext;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.joinable.Violations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpressionEvaluatorsTest {

    @Mock
    private ExpressionEvaluations expressionEvaluations;
    
    @Mock
    private ExpressionEvaluation expressionEvaluation;
    
    @Mock
    private StepContext<Object> stepContext;

    private ExpressionEvaluators expressionEvaluators;

    @BeforeEach
    void setUp() {
        expressionEvaluators = new ExpressionEvaluators(expressionEvaluations);
    }

    @Test
    void testReaderOf() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue("input")).thenReturn("result");

        Function<String, String> reader = expressionEvaluators.readerOf("test");
        String result = reader.apply("input");

        assertEquals("result", result);
    }

    @Test
    void testReaderOfWithNull() {
        when(expressionEvaluations.parse("invalid")).thenThrow(new RuntimeException());

        Function<String, String> reader = expressionEvaluators.readerOf("invalid");
        String result = reader.apply("input");

        assertNull(result);
    }

    @Test
    void testOptionalReaderOf() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue("input")).thenReturn("result");

        Function<String, Optional<String>> reader = expressionEvaluators.optionalReaderOf("test");
        Optional<String> result = reader.apply("input");

        assertTrue(result.isPresent());
        assertEquals("result", result.get());
    }

    @Test
    void testReaderOfOrThrow() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue("input")).thenReturn("result");

        Function<String, String> reader = expressionEvaluators.readerOfOrThrow("test", RuntimeException::new);
        String result = reader.apply("input");

        assertEquals("result", result);
    }

    @Test
    void testReaderOfOrThrowWithException() {
        when(expressionEvaluations.parse("invalid")).thenThrow(new RuntimeException());

        Function<String, String> reader = expressionEvaluators.readerOfOrThrow(
                "invalid", RuntimeException::new);

        assertThrows(RuntimeException.class, () -> reader.apply("input"));
    }

    @Test
    void testValidateOrThrowWithPredicate() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(any())).thenReturn("result");

        assertDoesNotThrow(() -> 
            expressionEvaluators.validateOrThrow("test", "info",
                    "result"::equals, RuntimeException::new));
    }

    @Test
    void testValidateOrThrowWithPredicateExpression() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);

        ExpressionEvaluation predicateEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.parse("#value == 'result'")).thenReturn(predicateEvaluation);
        when(predicateEvaluation.getValueWithVariables(any())).thenReturn(true);
        when(expressionEvaluation.getValue(any())).thenReturn("result");

        assertDoesNotThrow(() -> 
            expressionEvaluators.validateOrThrow("test", Map.of("var", "value"), 
                "#value == 'result'", RuntimeException::new));
    }

    @Test
    void testValidateWithPredicate() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(any())).thenReturn("result");

        Validation<Violations, String> validation = expressionEvaluators.validate("test", 
            Map.of("var", "value"), "result"::equals,
            () -> Violations.violate("error", "message"));

        assertTrue(validation.isValid());
    }

    @Test
    void testValidateWithPredicateExpression() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);

        ExpressionEvaluation booleanEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.parse("#root['valid'] == true")).thenReturn(booleanEvaluation);
        when(booleanEvaluation.getValueWithVariables(any())).thenReturn(true);
        when(expressionEvaluation.getValue(any()))
                .thenReturn(Map.of("id", 1, "name", "john", "valid", true));

        Validation<Violations, String> validation = expressionEvaluators
                .validate("test", Map.of(), "#root['valid'] == true",
                        () -> Violations.violate("error", "message"));

        assertTrue(validation.isValid());
    }

    @Test
    void testSupplyWriter() {
        when(expressionEvaluations.parse("property")).thenReturn(expressionEvaluation);

        BiConsumer<TestObject, String> writer = expressionEvaluators.supplyWriter(
                "property", (e, msg) -> new RuntimeException(msg, e));
        TestObject obj = new TestObject();

        assertDoesNotThrow(() -> writer.accept(obj, "value"));
        verify(expressionEvaluation).setValue(obj, "value");
    }

    @Test
    void testSupplyWriterWithVariable() {
        when(expressionEvaluations.parse("#var")).thenReturn(expressionEvaluation);

        BiConsumer<TestObject, String> writer = expressionEvaluators.supplyWriter(
                "#var", (e, msg) -> new RuntimeException(msg, e));
        TestObject obj = new TestObject();

        assertDoesNotThrow(() -> writer.accept(obj, "value"));
        verify(expressionEvaluation).setValue(any(Map.class), eq(obj), eq("value"));
    }

    @Test
    void testPredicateOf() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValueWithVariables(any())).thenReturn(true);

        Predicate<String> predicate = expressionEvaluators.predicateOf("test");
        boolean result = predicate.test("input");

        assertTrue(result);
    }

    @Test
    void testFunctionOf() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValueWithVariables(any())).thenReturn("result");

        Function<String, String> function = expressionEvaluators.functionOf("test");
        String result = function.apply("input");

        assertEquals("result", result);
    }

    @Test
    void testOptionalFunctionOf() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValueWithVariables(any())).thenReturn(Optional.of("result"));

        Function<String, Optional<String>> function = expressionEvaluators.optionalFunctionOf("test");
        Optional<String> result = function.apply("input");

        assertTrue(result.isPresent());
        assertEquals("result", result.get());
    }

    @Test
    void testCollectorOf() {
        Collector<CharSequence, ?, String> collector = Collectors.joining(",");
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue()).thenReturn(collector);

        Collector<String, ?, String> result = expressionEvaluators.collectorOf("test");

        assertEquals(collector, result);
    }

    @Test
    void testMaybeGetCollector() {
        Collector<CharSequence, ?, String> collector = Collectors.joining(",");
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue()).thenReturn(collector);

        Optional<Collector<Object, Object, Object>> result = expressionEvaluators
                .maybeGetCollector("test");

        assertTrue(result.isPresent());
        assertEquals(collector, result.get());
    }

    @Test
    void testEvaluate() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(any())).thenReturn("result");
        when(stepContext.getPayload()).thenReturn("payload");
        when(stepContext.withAttribute("resultName", "result")).thenReturn(stepContext);

        var behaviorStep = expressionEvaluators.evaluate("test", "resultName");
        var validation = behaviorStep.apply(stepContext);

        assertTrue(validation.isValid());
    }

    @Test
    void testEvaluateWithStepContext() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(any())).thenReturn("result");
        when(stepContext.getPayload()).thenReturn("payload");
        when(stepContext.withAttribute("resultName", "result")).thenReturn(stepContext);

        var behaviorStep = expressionEvaluators.evaluateWithStepContext("test", 
            Map.of("var", "value"), "resultName", ctx -> "contextValue");
        var validation = behaviorStep.apply(stepContext);

        assertTrue(validation.isValid());
    }

    @Test
    void testEvaluateWithVariables() {
        when(expressionEvaluations.parse("test")).thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(any())).thenReturn("result");
        when(stepContext.getPayload()).thenReturn("payload");
        when(stepContext.withAttribute("resultName", "result")).thenReturn(stepContext);

        var behaviorStep = expressionEvaluators.evaluate("test", Map.of("var", "value"), "resultName");
        var validation = behaviorStep.apply(stepContext);

        assertTrue(validation.isValid());
    }

    @Setter
    @Getter
    private static class TestObject {
        private String property;

    }
}