package org.dotspace.oofp.support.dsl.pipeline;

import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.utils.dsl.pipeline.BehaviorPipeline;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class BehaviorPipelineTest {

    private BehaviorPipeline<String> pipeline;

    @BeforeEach
    void setUp() {
        pipeline = BehaviorPipeline.steps();
    }

    @Test
    @DisplayName("Should create empty pipeline using static factory method")
    void shouldCreateEmptyPipeline() {
        assertNotNull(pipeline);
    }

    @Test
    @DisplayName("Should add step and return same pipeline instance")
    void shouldAddStepAndReturnSameInstance() {
        BehaviorStep<String> step = Validation::valid;
        
        BehaviorPipeline<String> result = pipeline.with(step);
        
        assertSame(pipeline, result);
    }

    @Test
    @DisplayName("Should execute single valid step successfully")
    void shouldExecuteSingleValidStepSuccessfully() {
        BehaviorStep<String> step = context -> Validation.valid(
            context.transit(context.getPayload().toUpperCase())
        );
        Function<StepContext<String>, String> resultApplier = StepContext::getPayload;
        
        Validation<Violations, String> result = pipeline
            .with(step)
            .apply("test", resultApplier);
        
        assertTrue(result.isValid());
        assertEquals("TEST", result.get().orElse(null));
    }

    @Test
    @DisplayName("Should execute multiple valid steps in sequence")
    void shouldExecuteMultipleValidStepsInSequence() {
        BehaviorStep<String> step1 = context -> Validation.valid(
            context.transit(context.getPayload().toUpperCase())
        );
        BehaviorStep<String> step2 = context -> Validation.valid(
            context.transit(context.getPayload() + "_PROCESSED")
        );
        Function<StepContext<String>, String> resultApplier = StepContext::getPayload;
        
        Validation<Violations, String> result = pipeline
            .with(step1)
            .with(step2)
            .apply("test", resultApplier);
        
        assertTrue(result.isValid());
        assertEquals("TEST_PROCESSED", result.get().orElse(null));
    }

    @Test
    @DisplayName("Should stop execution on first invalid step")
    void shouldStopExecutionOnFirstInvalidStep() {
        BehaviorStep<String> validStep = context -> Validation.valid(
            context.transit(context.getPayload().toUpperCase())
        );
        BehaviorStep<String> invalidStep = context -> Validation.invalid(
            Violations.violate("TEST_ERROR", "Test error message")
        );
        BehaviorStep<String> shouldNotExecute = context -> {
            fail("This step should not be executed");
            return Validation.valid(context);
        };
        Function<StepContext<String>, String> resultApplier = StepContext::getPayload;
        
        Validation<Violations, String> result = pipeline
            .with(validStep)
            .with(invalidStep)
            .with(shouldNotExecute)
            .apply("test", resultApplier);
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().orElse(Violations.empty()).collectMessages().contains("TEST_ERROR"));
    }

    @Test
    @DisplayName("Should handle empty pipeline")
    void shouldHandleEmptyPipeline() {
        Function<StepContext<String>, String> resultApplier = StepContext::getPayload;
        
        Validation<Violations, String> result = pipeline.apply("test", resultApplier);
        
        assertTrue(result.isValid());
        assertEquals("test", result.get().orElse(null));
    }

    @Test
    @DisplayName("Should apply result function to final context")
    void shouldApplyResultFunctionToFinalContext() {
        BehaviorStep<String> step = context -> Validation.valid(
            context.withAttribute("processed", true)
        );
        Function<StepContext<String>, Boolean> resultApplier = 
            context -> context.getAttributeOrDefault(
                    "processed", Boolean.class::cast, null);
        
        Validation<Violations, Boolean> result = pipeline
            .with(step)
            .apply("test", resultApplier);
        
        assertTrue(result.isValid());
        assertTrue(result.get().orElse(false));
    }

    @Test
    @DisplayName("Should throw NullPointerException for null input")
    void shouldThrowNullPointerExceptionForNullInput() {
        Function<StepContext<String>, String> resultApplier = StepContext::getPayload;
        
        assertThrows(NullPointerException.class, () -> 
            pipeline.apply(null, resultApplier)
        );
    }

    @Test
    @DisplayName("Should throw NullPointerException for null result applier")
    void shouldThrowNullPointerExceptionForNullResultApplier() {
        assertThrows(NullPointerException.class, () -> 
            pipeline.apply("test", null)
        );
    }

    @Test
    @DisplayName("Should preserve context violations through pipeline")
    void shouldPreserveContextViolationsThroughPipeline() {
        BehaviorStep<String> step1 = context -> Validation.valid(
            context.addViolation(Violations.violate("WARNING", "Warning message"))
        );
        BehaviorStep<String> step2 = context -> Validation.valid(
            context.transit(context.getPayload().toUpperCase())
        );
        Function<StepContext<String>, Violations> resultApplier = StepContext::getViolations;
        
        Validation<Violations, Violations> result = pipeline
            .with(step1)
            .with(step2)
            .apply("test", resultApplier);
        
        assertTrue(result.isValid());
        assertFalse(result.get().orElse(Violations.empty()).isEmpty());
    }

    @Test
    @DisplayName("Should handle step that modifies context attributes")
    void shouldHandleStepThatModifiesContextAttributes() {
        BehaviorStep<String> step1 = context -> Validation.valid(
            context.withAttribute("step1", "executed")
        );
        BehaviorStep<String> step2 = context -> Validation.valid(
            context.withAttribute("step2", "executed")
        );
        Function<StepContext<String>, java.util.Map<String, Object>> resultApplier = 
            context -> java.util.Map.of(
                "step1", context.getAttributeOrDefault("step1", String.class::cast, null),
                "step2", context.getAttributeOrDefault("step2", String.class::cast, null)
            );
        
        Validation<Violations, java.util.Map<String, Object>> result = pipeline
            .with(step1)
            .with(step2)
            .apply("test", resultApplier);
        
        assertTrue(result.isValid());
        java.util.Map<String, Object> attributes = result.get().orElse(java.util.Map.of());
        assertEquals("executed", attributes.get("step1"));
        assertEquals("executed", attributes.get("step2"));
    }

    @Test
    @DisplayName("Should handle complex step chain with different data types")
    void shouldHandleComplexStepChainWithDifferentDataTypes() {
        BehaviorStep<String> parseStep = context -> {
            try {
                int value = Integer.parseInt(context.getPayload());
                return Validation.valid(context.withAttribute("parsed", value));
            } catch (NumberFormatException e) {
                return Validation.invalid(Violations.violate(
                        "PARSE_ERROR", "Invalid number format"));
            }
        };
        
        BehaviorStep<String> processStep = context -> {
            Integer value = context.getAttributeOrDefault("parsed", Integer.class::cast, null);
            return Validation.valid(context.withAttribute("doubled", value * 2));
        };
        
        Function<StepContext<String>, Integer> resultApplier = 
            context -> context.getAttributeOrDefault(
                    "doubled", Integer.class::cast, null);
        
        Validation<Violations, Integer> result = pipeline
            .with(parseStep)
            .with(processStep)
            .apply("42", resultApplier);
        
        assertTrue(result.isValid());
        assertEquals(84, result.get().orElse(0));
    }

    @Test
    @DisplayName("Should handle invalid parsing in complex step chain")
    void shouldHandleInvalidParsingInComplexStepChain() {
        BehaviorStep<String> parseStep = context -> {
            try {
                int value = Integer.parseInt(context.getPayload());
                return Validation.valid(context.withAttribute("parsed", value));
            } catch (NumberFormatException e) {
                return Validation.invalid(Violations.violate("PARSE_ERROR", "Invalid number format"));
            }
        };
        
        Function<StepContext<String>, Integer> resultApplier = 
            context -> context.getAttributeOrDefault(
                    "parsed", Integer.class::cast, null);
        
        Validation<Violations, Integer> result = pipeline
            .with(parseStep)
            .apply("invalid", resultApplier);
        
        assertTrue(result.isInvalid());
        assertTrue(result.error().orElse(Violations.empty()).collectMessages().contains("PARSE_ERROR"));
    }
}