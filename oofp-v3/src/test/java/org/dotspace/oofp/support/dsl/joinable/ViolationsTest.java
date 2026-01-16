package org.dotspace.oofp.support.dsl.joinable;

import org.dotspace.oofp.enumeration.stepcontext.ViolationSeverity;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.GeneralViolation;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViolationsTest {

    @Test
    void testViolate() {
        Violations violations = Violations.violate("testValidation", "test message");
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.getViolationCollection().size());
        assertEquals("testValidation", violations.getViolationCollection().iterator().next().getValidationName());
        assertEquals(List.of("test message"), violations.getViolationCollection().iterator().next().getMessages());
    }

    @Test
    void testOf() {
        GeneralViolation violation = new GeneralViolation();
        violation.setValidationName("test");
        violation.setMessages(List.of("message"));
        
        Violations violations = Violations.of(violation);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.getViolationCollection().size());
        assertTrue(violations.getViolationCollection().contains(violation));
    }

    @Test
    void testEmpty() {
        Violations violations = Violations.empty();
        
        assertTrue(violations.isEmpty());
        assertEquals(0, violations.getViolationCollection().size());
    }

    @Test
    void testFrom() {
        GeneralViolation violation = new GeneralViolation();
        violation.setValidationName("test");
        
        Violations violations = Violations.from(List.of(violation));
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.getViolationCollection().size());
        assertTrue(violations.getViolationCollection().contains(violation));
    }

    @Test
    void testJoin() {
        Violations violations1 = Violations.violate("test1", "message1");
        Violations violations2 = Violations.violate("test2", "message2");
        
        Violations joined = violations1.join(violations2);
        
        assertEquals(2, joined.getViolationCollection().size());
    }

    @Test
    void testJoinWithDuplicates() {
        GeneralViolation violation = new GeneralViolation();
        violation.setValidationName("test");
        violation.setMessages(List.of("message"));
        
        Violations violations1 = Violations.of(violation);
        Violations violations2 = Violations.of(violation);
        
        Violations joined = violations1.join(violations2);
        
        assertEquals(1, joined.getViolationCollection().size());
    }

    @Test
    void testStream() {
        Violations violations = Violations.violate("test", "message");
        
        assertEquals(1, violations.stream().count());
    }

    @Test
    void testIsEmpty() {
        assertTrue(Violations.empty().isEmpty());
        assertFalse(Violations.violate("test", "message").isEmpty());
    }

    @Test
    void testTagStep() {
        Violations violations = Violations.violate("test", "message");
        
        violations.tagStep("stepName");
        
        assertEquals("stepName", violations.getViolationCollection().iterator().next().getStepName());
    }

    @Test
    void testToValidationValid() {
        StepContext<String> context = StepContext.<String>builder()
                .withPayload("123")
                .build();
        Violations violations = Violations.empty();
        
        Validation<Violations, StepContext<String>> validation = violations.toValidation(context);
        
        assertTrue(validation.isValid());
        assertEquals(context.getPayload(), validation.get()
                .map(StepContext::getPayload)
                .orElse(null));
    }

    @Test
    void testToValidationInvalid() {
        StepContext<String> context = StepContext.<String>builder()
                .build();
        Violations violations = Violations.violate("test", "message");
        
        Validation<Violations, StepContext<String>> validation = violations.toValidation(context);
        
        assertTrue(validation.isInvalid());
        assertEquals(violations, validation.error().get());
    }

    @Test
    void testGetInvalidValidation() {
        Exception exception = new RuntimeException("test exception");
        
        Validation<Violations, StepContext<String>> validation = 
            Violations.getInvalidValidation("testResult", exception);
        
        assertTrue(validation.isInvalid());
        Violations violations = validation.error().get();
        assertEquals(1, violations.getViolationCollection().size());
        assertEquals("testResult", violations.getViolationCollection().iterator().next().getValidationName());
    }

    @Test
    void testGetInvalidValidationWithNullMessage() {
        Exception exception = new RuntimeException((String) null);
        
        Validation<Violations, StepContext<String>> validation = 
            Violations.getInvalidValidation("testResult", exception);
        
        assertTrue(validation.isInvalid());
        Violations violations = validation.error().get();
        assertTrue(violations.getViolationCollection().iterator().next().getMessages().get(0)
            .contains("exception message is null"));
    }

    @Test
    void testGetSize() {
        Violations empty = Violations.empty();
        Violations single = Violations.violate("test", "message");
        
        assertEquals("0", empty.getSize());
        assertEquals("1", single.getSize());
    }

    @Test
    void testCollectMessages() {
        Violations violations = Violations.violate("testValidation", "test message");
        
        String messages = violations.collectMessages();
        
        assertTrue(messages.contains("testValidation"));
        assertTrue(messages.contains("test message"));
    }

    @Test
    void testCollectMessagesWithSeverity() {
        GeneralViolation violation = new GeneralViolation();
        violation.setValidationName("test");
        violation.setMessages(List.of("message"));
        violation.getOptions().put(GeneralViolation.SEVERITY, ViolationSeverity.ERROR);
        
        Violations violations = Violations.of(violation);
        String messages = violations.collectMessages();
        
        assertTrue(messages.contains("(ERROR)"));
    }

    @Test
    void testEquals() {
        Violations violations1 = Violations.violate("test", "message");
        Violations violations2 = Violations.violate("test", "message");
        
        assertEquals(violations1, violations2);
    }

    @Test
    void testNotEquals() {
        Violations violations1 = Violations.violate("test1", "message");
        Violations violations2 = Violations.violate("test2", "message");
        
        assertNotEquals(violations1, violations2);
        assertNotEquals(null, violations1);
        assertNotEquals("string", violations1.collectMessages());
    }

    @Test
    void testHashCode() {
        Violations violations1 = Violations.violate("test", "message");
        Violations violations2 = Violations.violate("test", "message");
        
        assertEquals(violations1.hashCode(), violations2.hashCode());
    }
}