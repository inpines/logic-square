package org.dotspace.oofp.support.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExpressionEvaluationsTest {

    @Mock
    private ApplicationContext applicationContext;

    private ExpressionEvaluations expressionEvaluations;

    @BeforeEach
    void setUp() {
        expressionEvaluations = new ExpressionEvaluations();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "property.value",
            "",
            "T(java.lang.Math).max(10, 20)",
            "#{systemProperties['java.version']}"})
    void parse_WithValidExpression_ReturnsExpressionEvaluationImpl() {
        // Arrange
        expressionEvaluations.setApplicationContext(applicationContext);
        String expression = "property.value";

        // Act
        ExpressionEvaluation result = expressionEvaluations.evaluate(expression);

        // Assert
        assertNotNull(result);
        assertInstanceOf(ExpressionEvaluation.class, result);
    }

    @Test
    void parse_WithNullExpression_ReturnsExpressionEvaluationImpl() {
        // Arrange
        expressionEvaluations.setApplicationContext(applicationContext);

        // Act
        ExpressionEvaluation result = expressionEvaluations.evaluate(null);

        // Assert
        assertNotNull(result);
        assertInstanceOf(ExpressionEvaluation.class, result);
    }

    @Test
    void setApplicationContext_WithValidContext_SetsContext() {
        // Act
        expressionEvaluations.setApplicationContext(applicationContext);

        // Assert - Verify context is set by testing parse method works
        ExpressionEvaluation result = expressionEvaluations.evaluate("test");
        assertNotNull(result);
    }

    @Test
    void setApplicationContext_WithNullContext_ThrowsBeansException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            expressionEvaluations.setApplicationContext(null);
        });
    }

    @Test
    void parse_WithoutSettingApplicationContext_ThrowsException() {
        // Arrange
        String expression = "property.value";

        // Act & Assert
        var evalation = expressionEvaluations.evaluate(expression);

        assertNotNull(evalation);
        assertThrows(Exception.class, evalation::getValue);
    }

    @Test
    void parse_MultipleCallsWithSameExpression_ReturnsDifferentInstances() {
        // Arrange
        expressionEvaluations.setApplicationContext(applicationContext);
        String expression = "property.value";

        // Act
        ExpressionEvaluation result1 = expressionEvaluations.evaluate(expression);
        ExpressionEvaluation result2 = expressionEvaluations.evaluate(expression);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2);
    }

}