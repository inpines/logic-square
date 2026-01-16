package org.dotspace.oofp.support.validator;

import org.dotspace.oofp.enumeration.stepcontext.ViolationSeverity;

import org.dotspace.oofp.model.dto.behaviorstep.GeneralViolation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneralViolationTest {

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @Mock
    private Path propertyPath;

    private GeneralViolation violation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        violation = new GeneralViolation();
    }

    @Test
    void testConstants() {
        assertEquals("severe", GeneralViolation.SEVERE);
        assertEquals("warning", GeneralViolation.WARNING);
        assertEquals("severity", GeneralViolation.SEVERITY);
        assertEquals(" (%d) %s", GeneralViolation.ORDER_CONTENT);
    }

    @Test
    void testSettersAndGetters() {
        violation.setValidationName("testValidation");
        violation.setStepName("testStep");
        violation.setMessages(List.of("message1", "message2"));

        assertEquals("testValidation", violation.getValidationName());
        assertEquals("testStep", violation.getStepName());
        assertEquals(List.of("message1", "message2"), violation.getMessages());
    }

    @Test
    void testIsSevere_WhenSevereOptionIsTrue() {
        violation.getOptions().put(GeneralViolation.SEVERE, true);
        assertTrue(violation.isSevere());
    }

    @Test
    void testIsSevere_WhenSevereOptionIsFalse() {
        violation.getOptions().put(GeneralViolation.SEVERE, false);
        assertFalse(violation.isSevere());
    }

    @Test
    void testIsSevere_WhenSevereOptionIsNotSet() {
        assertFalse(violation.isSevere());
    }

    @Test
    void testIsWarning_WhenWarningOptionIsTrue() {
        violation.getOptions().put(GeneralViolation.WARNING, true);
        assertTrue(violation.isWarning());
    }

    @Test
    void testIsWarning_WhenWarningOptionIsFalse() {
        violation.getOptions().put(GeneralViolation.WARNING, false);
        assertFalse(violation.isWarning());
    }

    @Test
    void testIsWarning_WhenWarningOptionIsNotSet() {
        assertFalse(violation.isWarning());
    }

    @Test
    void testGetSeverity_WhenSeverityIsSet() {
        violation.getOptions().put(GeneralViolation.SEVERITY, ViolationSeverity.ERROR);
        assertEquals(ViolationSeverity.ERROR, violation.getSeverity());
    }

    @Test
    void testGetSeverity_WhenSeverityIsNotSet() {
        assertEquals(ViolationSeverity.UNSPECIFIED, violation.getSeverity());
    }

    @Test
    void testFromConstraint_WithSingleViolation() {
        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("field1");
        when(constraintViolation.getMessage()).thenReturn("Validation failed");

        Collection<ConstraintViolation<Object>> violations = List.of(constraintViolation);
        Map<String, Object> options = new HashMap<>();

        String result = GeneralViolation.fromConstraint(violations, options);

        assertEquals(" (1) Validation failed", result);
    }

    @Test
    void testFromConstraint_WithMultipleViolations() {
        ConstraintViolation<Object> violation2 = mock(ConstraintViolation.class);
        Path propertyPath2 = mock(Path.class);

        when(constraintViolation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("field1");
        when(constraintViolation.getMessage()).thenReturn("First error");

        when(violation2.getPropertyPath()).thenReturn(propertyPath2);
        when(propertyPath2.toString()).thenReturn("field2");
        when(violation2.getMessage()).thenReturn("Second error");

        Collection<ConstraintViolation<Object>> violations = List.of(constraintViolation, violation2);
        Map<String, Object> options = new HashMap<>();

        String result = GeneralViolation.fromConstraint(violations, options);

        assertEquals(" (1) First error\n (2) Second error", result);
    }

    @Test
    void testGetViolationMessages_WithEmptyCollection() {
        Collection<GeneralViolation> violations = Collections.emptyList();
        Map<String, Object> options = new HashMap<>();

        String result = GeneralViolation.getViolationMessages(violations, options);

        assertEquals("", result);
    }

    @Test
    void testGetViolationMessages_WithNullCollection() {
        Map<String, Object> options = new HashMap<>();

        String result = GeneralViolation.getViolationMessages(null, options);

        assertEquals("", result);
    }

    @Test
    void testGetViolationMessages_WithSingleViolation() {
        GeneralViolation vio = new GeneralViolation();
        vio.setMessages(List.of("Test message"));

        Collection<GeneralViolation> violations = List.of(vio);
        Map<String, Object> options = new HashMap<>();

        String result = GeneralViolation.getViolationMessages(violations, options);

        assertEquals(" (1) Test message", result);
    }

    @Test
    void testGetViolationMessages_WithMultipleMessages() {
        GeneralViolation vio = new GeneralViolation();
        vio.setMessages(List.of("Message 1", "Message 2"));

        Collection<GeneralViolation> violations = List.of(vio);
        Map<String, Object> options = new HashMap<>();

        String result = GeneralViolation.getViolationMessages(violations, options);

        assertEquals(" (1) Message 1, Message 2", result);
    }

    @Test
    void testGetViolationMessages_WithPropertyPath() {
        GeneralViolation vio = new GeneralViolation();
        vio.setValidationName("fieldName");
        vio.setMessages(List.of("Test message"));

        Collection<GeneralViolation> violations = List.of(vio);
        Map<String, Object> options = Map.of("withPropertyPath", true);

        String result = GeneralViolation.getViolationMessages(violations, options);

        assertEquals(" (1) Test message {fieldName}", result);
    }

    @Test
    void testGetViolationMessages_WithMultipleViolations() {
        GeneralViolation violation1 = new GeneralViolation();
        violation1.setMessages(List.of("First error"));

        GeneralViolation violation2 = new GeneralViolation();
        violation2.setMessages(List.of("Second error"));

        Collection<GeneralViolation> violations = List.of(violation1, violation2);
        Map<String, Object> options = new HashMap<>();

        String result = GeneralViolation.getViolationMessages(violations, options);

        assertEquals(" (1) First error\n (2) Second error", result);
    }

    @Test
    void testGetViolationMessages_WithPropertyPathFalse() {
        GeneralViolation vio = new GeneralViolation();
        vio.setValidationName("fieldName");
        vio.setMessages(List.of("Test message"));

        Collection<GeneralViolation> violations = List.of(vio);
        Map<String, Object> options = Map.of("withPropertyPath", false);

        String result = GeneralViolation.getViolationMessages(violations, options);

        assertEquals(" (1) Test message", result);
    }

    @Test
    void testEqualsAndHashCode() {
        GeneralViolation violation1 = new GeneralViolation();
        violation1.setValidationName("test");
        violation1.setStepName("step");
        violation1.setMessages(List.of("message"));

        GeneralViolation violation2 = new GeneralViolation();
        violation2.setValidationName("test");
        violation2.setStepName("step");
        violation2.setMessages(List.of("message"));

        assertEquals(violation1, violation2);
        assertEquals(violation1.hashCode(), violation2.hashCode());
    }

    @Test
    void testToString() {
        GeneralViolation vio = new GeneralViolation();
        vio.setValidationName("test");
        vio.setStepName("step");
        vio.setMessages(List.of("message"));

        String result = vio.toString();

        assertNotNull(result);
        assertTrue(result.contains("GeneralViolation"));
    }
}