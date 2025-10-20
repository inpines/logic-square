package org.dotspace.oofp.support.expression;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExpressionEvaluationTest {

    @Mock
    private ConfigurableApplicationContext applicationContext;

    private ExpressionEvaluation expressionEvaluation;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    void testGetValueWithClass() {
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "2 + 3");
        
        Integer result = expressionEvaluation.getValue(Integer.class, new Object());
        
        assertEquals(5, result);
    }

    @Test
    void testGetValueWithClassAndVariables() {
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "#var1 + #var2");
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1", 10);
        variables.put("var2", 20);
        
        Integer result = expressionEvaluation.getValue(Integer.class, variables, new Object());
        
        assertEquals(30, result);
    }

    @Test
    void testGetValueWithVariablesAndRoot() {
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "#name");
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "test");
        
        String result = expressionEvaluation.getValueWithVariables(variables, new Object());
        
        assertEquals("test", result);
    }

    @Test
    void testGetValueWithVariablesOnly() {
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "#count * 2");
        Map<String, Object> variables = new HashMap<>();
        variables.put("count", 5);
        
        Integer result = expressionEvaluation.getValueWithVariables(variables);
        
        assertEquals(10, result);
    }

    @Test
    void testGetValueWithRoot() {
        TestObject root = new TestObject("hello");
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "value.toUpperCase()");
        
        String result = expressionEvaluation.getValue(root);
        
        assertEquals("HELLO", result);
    }

    @Test
    void testGetValueNoParameters() {
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "'constant'");
        
        String result = expressionEvaluation.getValue();
        
        assertEquals("constant", result);
    }

    @Test
    void testSetValueWithVariables() {
        TestObject root = new TestObject("initial");
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "value");
        Map<String, Object> variables = new HashMap<>();
        
        assertDoesNotThrow(() -> 
            expressionEvaluation.setValue(variables, root, "updated")
        );
        assertEquals("updated", root.getValue());
    }

    @Test
    void testSetValueWithRoot() {
        TestObject root = new TestObject("initial");
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "value");
        
        assertDoesNotThrow(() -> 
            expressionEvaluation.setValue(root, "changed")
        );
        assertEquals("changed", root.getValue());
    }

    @Test
    void testNullExpressionText() {
        expressionEvaluation = new ExpressionEvaluation(applicationContext, null);
        
        Object result = expressionEvaluation.getValue();
        
        assertNull(result);
    }

    @Test
    void testEmptyExpressionText() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExpressionEvaluation(applicationContext, ""));
    }

    @Test
    void testComplexExpression() {
        TestObject root = new TestObject("world");
        expressionEvaluation = new ExpressionEvaluation(applicationContext, "'Hello ' + value + '!'");
        
        String result = expressionEvaluation.getValue(root);
        
        assertEquals("Hello world!", result);
    }

    @Setter
    @Getter
    private static class TestObject {
        private String value;

        public TestObject(String value) {
            this.value = value;
        }

    }
}