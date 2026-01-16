package org.dotspace.oofp.support.validator.constraint;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.dotspace.oofp.support.expression.ExpressionEvaluation;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;

@ExtendWith(MockitoExtension.class)
class CorrelationValidatorTest {

    @Mock
    private ExpressionEvaluations expressionEvaluations;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintViolationBuilder violationBuilder;

    @Mock
    private NodeBuilderCustomizableContext nodeBuilder;

    @Mock
    private ExpressionEvaluation expressionEvaluation;

    private CorrelationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CorrelationValidator(expressionEvaluations);

        lenient().when(context.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(violationBuilder);
        lenient().when(violationBuilder.addPropertyNode(anyString()))
            .thenReturn(nodeBuilder);
        lenient().when(nodeBuilder.addConstraintViolation())
            .thenReturn(context);
    }

    @Test
    void testIsValid_NoMandatoryFields_ReturnsTrue() {
        TestObject target = new TestObject();

        boolean result = validator.isValid(target, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_MandatoryFieldPresent_WhenConditionTrue_ReturnsTrue() {
        TestObjectWithMandatory target = new TestObjectWithMandatory();
        target.setAge(15);
        target.setSchool("Test School");

        ExpressionEvaluation ageExpressionEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("age < 20"))
            .thenReturn(ageExpressionEvaluation);
        when(ageExpressionEvaluation.getValue(target))
            .thenReturn(true);
        when(expressionEvaluations.evaluate("school"))
            .thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(target))
            .thenReturn("Test School");

        boolean result = validator.isValid(target, context);

        assertTrue(result);
    }

    @Test
    void testIsValid_MandatoryFieldMissing_WhenConditionTrue_ReturnsFalse() {
        TestObjectWithMandatory target = new TestObjectWithMandatory();
        target.setAge(15);

        ExpressionEvaluation ageExpressionEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("age < 20"))
            .thenReturn(ageExpressionEvaluation);
        when(ageExpressionEvaluation.getValue(target))
            .thenReturn(true);

        when(expressionEvaluations.evaluate("school"))
            .thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(target))
            .thenReturn(null);

        boolean result = validator.isValid(target, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("此欄位必須輸入");
        verify(violationBuilder).addPropertyNode("school");
    }

    @Test
    void testIsValid_FieldShouldBeEmpty_ButHasValue_ReturnsFalse() {
        TestObjectWithEmpty target = new TestObjectWithEmpty();
        target.setAge(25);
        target.setSchool("Should be empty");

        ExpressionEvaluation ageExpressionEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("age >= 20"))
            .thenReturn(ageExpressionEvaluation);
        when(ageExpressionEvaluation.getValue(target))
            .thenReturn(true);

        when(expressionEvaluations.evaluate("school"))
            .thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(target))
            .thenReturn("Should be empty");

        boolean result = validator.isValid(target, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("此欄位不可輸入");
        verify(violationBuilder).addPropertyNode("school");
    }

    @Test
    void testIsValid_ValueTestFails_ReturnsFalse() {
        TestObjectWithValueTest target = new TestObjectWithValueTest();
        target.setAge(25);
        target.setJob("invalid");

        ExpressionEvaluation ageExpressionEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("age >= 20"))
            .thenReturn(ageExpressionEvaluation);
        when(ageExpressionEvaluation.getValue(target))
            .thenReturn(true);

        ExpressionEvaluation jobExpressionEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("job"))
            .thenReturn(jobExpressionEvaluation);
        when(jobExpressionEvaluation.getValue(target))
            .thenReturn("invalid");

        boolean result = validator.isValid(target, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("欄位內容檢核失敗");
        verify(violationBuilder).addPropertyNode("job");
    }

    @Test
    void testIsValid_WhenConditionFalse_SkipsValidation_ReturnsTrue() {
        TestObjectWithMandatory target = new TestObjectWithMandatory();
        target.setAge(25);

        when(expressionEvaluations.evaluate("age < 20"))
            .thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(target))
            .thenReturn(false);

        boolean result = validator.isValid(target, context);

        assertTrue(result);
        verify(expressionEvaluations, never()).evaluate("school");
    }

    @Test
    void testIsValid_EmptyStringValue_TreatedAsNotPresent() {
        TestObjectWithMandatory target = new TestObjectWithMandatory();
        target.setAge(15);
        target.setSchool("");

        ExpressionEvaluation ageExpressionEvaluation = mock(ExpressionEvaluation.class);
        when(expressionEvaluations.evaluate("age < 20"))
            .thenReturn(ageExpressionEvaluation);
        when(ageExpressionEvaluation.getValue(target))
            .thenReturn(true);
        when(expressionEvaluations.evaluate("school"))
            .thenReturn(expressionEvaluation);
        when(expressionEvaluation.getValue(target))
            .thenReturn("");

        boolean result = validator.isValid(target, context);

        assertFalse(result);
        verify(context).buildConstraintViolationWithTemplate("此欄位必須輸入");
    }

    // Test classes
    @Setter
    @Getter
    static class TestObject {
        private String name;

    }

    @Setter
    @Getter
    static class TestObjectWithMandatory {
        private Integer age;

        @MandatoryField(cases = { @MandatoryFieldCase(when = "age < 20") })
        private String school;

    }

    @Setter
    @Getter
    static class TestObjectWithEmpty {
        private Integer age;

        @MandatoryField(cases = { @MandatoryFieldCase(when = "age >= 20", present = false, empty = true) })
        private String school;

    }

    @Setter
    @Getter
    static class TestObjectWithValueTest {
        private Integer age;

        @MandatoryField(cases = { @MandatoryFieldCase(when = "age >= 20",
            valueTest = "'teacher'.equals($$) or 'worker'.equals($$)") })
        private String job;

    }
}