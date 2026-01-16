package org.dotspace.oofp.support.validator;

import org.dotspace.oofp.support.expression.ExpressionEvaluation;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.validator.constraint.MandatoryField;
import org.dotspace.oofp.support.validator.constraint.MandatoryFieldCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneralCorrelationValidatorTest {

    @Mock
    private ExpressionEvaluations expressionEvaluations;

    @Mock
    private ExpressionEvaluation booleanEvaluation;

    @Mock
    private ExpressionEvaluation objectEvaluation;

    private GeneralCorrelationValidator<TestObject> validator;
    private GeneralCorrelationValidator<TestObjectNoAnnotations> validatorNoAnnot;
    private GeneralCorrelationValidator<TestObjectWithEmptyConstraint> validatorWithEmptyConstraint;
    private GeneralCorrelationValidator<TestObjectWithValueTest> validatorWithValueTest;

    @BeforeEach
    void setUp() {
        validator = new GeneralCorrelationValidator<>(expressionEvaluations);
        validatorNoAnnot = new GeneralCorrelationValidator<>(expressionEvaluations);
        validatorWithEmptyConstraint = new GeneralCorrelationValidator<>(expressionEvaluations);
        validatorWithValueTest = new GeneralCorrelationValidator<>(expressionEvaluations);
    }

    @Test
    void validate_WithNoMandatoryFields_ReturnsTrue() {
        TestObjectNoAnnotations testObj = new TestObjectNoAnnotations();
        testObj.field1 = "value";

        boolean result = validatorNoAnnot.validate(testObj);

        assertTrue(result);
    }

    @Test
    void validate_WithValidMandatoryField_ReturnsTrue() {
        TestObject testObj = new TestObject();
        testObj.requiredField = "value";

        when(expressionEvaluations.evaluate("true")).thenReturn(booleanEvaluation);
        when(booleanEvaluation.getValue(testObj)).thenReturn(true);
        when(expressionEvaluations.evaluate("requiredField")).thenReturn(objectEvaluation);
        when(objectEvaluation.getValue(testObj)).thenReturn("value");

        boolean result = validator.validate(testObj);

        assertTrue(result);
    }

    @Test
    void validate_WithMissingRequiredField_ReturnsFalse() {
        TestObject testObj = new TestObject();
        testObj.requiredField = null;

        when(expressionEvaluations.evaluate("true")).thenReturn(booleanEvaluation);
        when(booleanEvaluation.getValue(testObj)).thenReturn(true);
        when(expressionEvaluations.evaluate("requiredField")).thenReturn(objectEvaluation);
        when(objectEvaluation.getValue(testObj)).thenReturn(null);

        boolean result = validator.validate(testObj);

        assertFalse(result);
    }

    @Test
    void validate_WithEmptyRequiredField_ReturnsFalse() {
        TestObject testObj = new TestObject();
        testObj.requiredField = "";

        when(expressionEvaluations.evaluate("true")).thenReturn(booleanEvaluation);
        when(booleanEvaluation.getValue(testObj)).thenReturn(true);
        when(expressionEvaluations.evaluate("requiredField")).thenReturn(objectEvaluation);
        when(objectEvaluation.getValue(testObj)).thenReturn("");

        boolean result = validator.validate(testObj);

        assertFalse(result);
    }

    @Test
    void validate_WithFieldThatShouldBeEmpty_ReturnsFalse() {
        TestObjectWithEmptyConstraint testObj = new TestObjectWithEmptyConstraint();
        testObj.shouldBeEmptyField = "value";

        when(expressionEvaluations.evaluate("true")).thenReturn(booleanEvaluation);
        when(booleanEvaluation.getValue(testObj)).thenReturn(true);
        when(expressionEvaluations.evaluate("shouldBeEmptyField")).thenReturn(objectEvaluation);
        when(objectEvaluation.getValue(testObj)).thenReturn("value");

        boolean result = validatorWithEmptyConstraint.validate(testObj);

        assertFalse(result);
    }

    @Test
    void validate_WithFailedValueTest_ReturnsFalse() {
        TestObjectWithValueTest testObj = new TestObjectWithValueTest();
        testObj.testField = "invalid";

        when(expressionEvaluations.evaluate("true")).thenReturn(booleanEvaluation);
        when(booleanEvaluation.getValue(testObj)).thenReturn(true);
        when(expressionEvaluations.evaluate("testField")).thenReturn(objectEvaluation);
        when(objectEvaluation.getValue(testObj)).thenReturn("invalid");
        ExpressionEvaluation booleanTestEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("testField.length() > 5")).thenReturn(booleanTestEvaluation);
        when(booleanTestEvaluation.getValue(testObj)).thenReturn(false);

        boolean result = validatorWithValueTest.validate(testObj);

        assertFalse(result);
    }

    @Test
    void validate_WithConditionNotMet_ReturnsTrue() {
        TestObject testObj = new TestObject();
        testObj.requiredField = null;

        when(expressionEvaluations.evaluate("true")).thenReturn(booleanEvaluation);
        when(booleanEvaluation.getValue(testObj)).thenReturn(false);

        boolean result = validator.validate(testObj);

        assertTrue(result);
    }

    // Test classes
    static class TestObject {
        @MandatoryField(cases = @MandatoryFieldCase(when = "true"))
        public String requiredField;
    }

    static class TestObjectNoAnnotations {
        public String field1;
    }

    static class TestObjectWithEmptyConstraint {
        @MandatoryField(cases = @MandatoryFieldCase(when = "true", empty = true))
        public String shouldBeEmptyField;
    }

    static class TestObjectWithValueTest {
        @MandatoryField(cases = @MandatoryFieldCase(when = "true", valueTest = "$$.length() > 5"))
        public String testField;
    }
}