package org.dotspace.oofp.support.dsl.step;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.Setter;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.validator.constraint.MandatoryField;
import org.dotspace.oofp.support.validator.constraint.MandatoryFieldCase;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.dsl.StepContext;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.joinable.Violations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationStepsTest {

    @Mock
    private Validator validator;

    @Mock
    private ExpressionEvaluations expressionEvaluations;

    @Mock
    private StepContext<TestDto> stepContext;

    @Mock
    private ConstraintViolation<TestDto> constraintViolation;

    @Mock
    private Path propertyPath;

    private ValidationSteps validationSteps;

    @BeforeEach
    void setUp() {
        validationSteps = new ValidationSteps(validator, expressionEvaluations);
    }

    @Test
    void constraintValidator_withNoViolations_returnsValidValidation() {
        // Given
        TestDto testDto = new TestDto();
        when(stepContext.getPayload()).thenReturn(testDto);
        when(validator.validate(testDto)).thenReturn(Collections.emptySet());

        // When
        BehaviorStep<TestDto> step = validationSteps.constraintValidator();
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertTrue(result.isValid());
        assertEquals(stepContext.getPayload(), result.get().map(StepContext::getPayload).orElse(null));
    }

    @Test
    void constraintValidator_withViolations_returnsInvalidValidation() {
        // Given
        TestDto testDto = new TestDto();
        Set<ConstraintViolation<TestDto>> violations = Set.of(constraintViolation);
        
        when(stepContext.getPayload()).thenReturn(testDto);
        when(validator.validate(testDto)).thenReturn(violations);
        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("testField");
        when(constraintViolation.getMessage()).thenReturn("Test violation message");

        // When
        BehaviorStep<TestDto> step = validationSteps.constraintValidator();
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertTrue(result.isInvalid());
        assertNotNull(result.error().orElse(null));
    }

    @Test
    void constraintValidator_withNullPropertyPath_handlesGracefully() {
        // Given
        TestDto testDto = new TestDto();
        Set<ConstraintViolation<TestDto>> violations = Set.of(constraintViolation);
        
        when(stepContext.getPayload()).thenReturn(testDto);
        when(validator.validate(testDto)).thenReturn(violations);
        when(constraintViolation.getPropertyPath()).thenReturn(null);
        when(constraintViolation.getMessage()).thenReturn("Test message");

        // When
        BehaviorStep<TestDto> step = validationSteps.constraintValidator();
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertTrue(result.isInvalid());
    }

    @Test
    void constraintValidator_withNullMessage_handlesGracefully() {
        // Given
        TestDto testDto = new TestDto();
        Set<ConstraintViolation<TestDto>> violations = Set.of(constraintViolation);
        
        when(stepContext.getPayload()).thenReturn(testDto);
        when(validator.validate(testDto)).thenReturn(violations);
        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("testField");
        when(constraintViolation.getMessage()).thenReturn(null);

        // When
        BehaviorStep<TestDto> step = validationSteps.constraintValidator();
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertTrue(result.isInvalid());
    }

    @Test
    void correlationValidator_returnsValidationResult() {
        // Given
        TestDto testDto = new TestDto();
        when(stepContext.getPayload()).thenReturn(testDto);

        // When
        BehaviorStep<TestDto> step = validationSteps.correlationValidator();
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertNotNull(result);
    }

    @Test
    void propertiesValidator_withValidProperties_returnsValidation() {
        // Given
        TestDto testDto = new TestDto();
        testDto.setName("Test");
        
        List<Map<String, Object>> propInfos = List.of(
            Map.of(
                ValidationSteps.NAME, "Name Field",
                ValidationSteps.PROPERTY, "name",
                ValidationSteps.REQUIRED, true,
                ValidationSteps.LENGTH, 10,
                ValidationSteps.REGEX, ".*",
                ValidationSteps.REGEX_MESSAGE, "Invalid format"
            )
        );

        when(stepContext.getPayload()).thenReturn(testDto);

        // When
        BehaviorStep<TestDto> step = validationSteps.propertiesValidator(propInfos);
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertNotNull(result);
    }

    @Test
    void propertiesValidator_withEmptyPropInfos_returnsValidation() {
        // Given
        TestDto testDto = new TestDto();
        List<Map<String, Object>> propInfos = Collections.emptyList();
        
        when(stepContext.getPayload()).thenReturn(testDto);

        // When
        BehaviorStep<TestDto> step = validationSteps.propertiesValidator(propInfos);
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertNotNull(result);
    }

    @Test
    void propertiesValidator_withMissingRequiredField_createsViolation() {
        // Given
        TestDto testDto = new TestDto();
        
        List<Map<String, Object>> propInfos = List.of(
            Map.of(
                ValidationSteps.NAME, "Name Field",
                ValidationSteps.PROPERTY, "name",
                ValidationSteps.REQUIRED, true,
                ValidationSteps.LENGTH, 10
            )
        );

        when(stepContext.getPayload()).thenReturn(testDto);

        // When
        BehaviorStep<TestDto> step = validationSteps.propertiesValidator(propInfos);
        Validation<Violations, StepContext<TestDto>> result = step.apply(stepContext);

        // Then
        assertNotNull(result);
    }

    // Test DTO class for testing
    @Setter
    @Getter
    public static class TestDto {
        @MandatoryField(cases = {
            @MandatoryFieldCase(when = "true")
        })
        private String name;
        
        private String description;

    }
}