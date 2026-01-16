package org.dotspace.oofp.support.dsl.pipeline;

import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.GeneralViolation;
import org.dotspace.oofp.utils.dsl.pipeline.ViolationFilters;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class ViolationFiltersTest {

    @Test
    void keepAll_returnsIdentityFunction() {
        // Given
        Violations violations = createTestViolations();
        
        // When
        Function<Violations, Violations> filter = ViolationFilters.keepAll();
        Violations result = filter.apply(violations);
        
        // Then
        assertSame(violations, result);
        assertEquals(2, result.stream().count());
    }

    @Test
    void dropAll_returnsEmptyViolations() {
        // Given
        Violations violations = createTestViolations();
        
        // When
        Function<Violations, Violations> filter = ViolationFilters.dropAll();
        Violations result = filter.apply(violations);
        
        // Then
        assertTrue(result.isEmpty());
        assertEquals(0, result.stream().count());
    }

    @Test
    void onlySevere_filtersOnlySevereViolations() {
        // Given
        Violations violations = createMixedSeverityViolations();
        
        // When
        Function<Violations, Violations> filter = ViolationFilters.onlySevere();
        Violations result = filter.apply(violations);
        
        // Then
        assertEquals(1, result.stream().count());
        assertTrue(result.stream().allMatch(GeneralViolation::isSevere));
    }

    @Test
    void ignoreWarnings_filtersOutWarnings() {
        // Given
        Violations violations = createMixedWarningViolations();
        
        // When
        Function<Violations, Violations> filter = ViolationFilters.ignoreWarnings();
        Violations result = filter.apply(violations);
        
        // Then
        assertEquals(1, result.stream().count());
        assertTrue(result.stream().noneMatch(GeneralViolation::isWarning));
    }

    private Violations createTestViolations() {
        GeneralViolation violation1 = GeneralBuilders.supply(GeneralViolation::new)
                .with(WriteOperations.set(GeneralViolation::setValidationName, "test1"))
                .with(WriteOperations.set(GeneralViolation::setMessages, List.of("Test message 1")))
                .build();

        GeneralViolation violation2 = GeneralBuilders.supply(GeneralViolation::new)
                .with(WriteOperations.set(GeneralViolation::setValidationName, "test2"))
                .with(WriteOperations.set(GeneralViolation::setMessages, List.of("Test message 2")))
                .build();

        return Violations.from(List.of(violation1, violation2));
    }

    private Violations createMixedSeverityViolations() {
        GeneralViolation severeViolation = createViolationWithSeverity(true, false);
        GeneralViolation normalViolation = createViolationWithSeverity(false, false);

        return Violations.from(List.of(severeViolation, normalViolation));
    }

    private Violations createMixedWarningViolations() {
        GeneralViolation warningViolation = createViolationWithSeverity(false, true);
        GeneralViolation normalViolation = createViolationWithSeverity(false, false);

        return Violations.from(List.of(warningViolation, normalViolation));
    }

    private GeneralViolation createViolationWithSeverity(boolean severe, boolean warning) {
        GeneralViolation violation = GeneralBuilders.supply(GeneralViolation::new)
                .with(WriteOperations.set(GeneralViolation::setValidationName, "test"))
                .with(WriteOperations.set(GeneralViolation::setMessages, List.of("Test message")))
                .build();

        if (severe) {
            violation.getOptions().put(GeneralViolation.SEVERE, true);
        }
        if (warning) {
            violation.getOptions().put(GeneralViolation.WARNING, true);
        }

        return violation;
    }
}