package org.dotspace.oofp.utils.oofp.functional.monad.validation;

import org.dotspace.oofp.utils.dsl.Joinable;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.functional.monad.validation.ValidationUtils;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Getter
    static class TestError implements Joinable<TestError> {
        private final StringBuilder messageBuilder;

        public TestError(String message) {
            this.messageBuilder = new StringBuilder(message);
        }

        public String getMessage() {
            return messageBuilder.toString();
        }

        @Override
        public TestError join(TestError other) {
            this.messageBuilder.append(", ")
                    .append(other.messageBuilder);
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestError testError = (TestError) obj;
            return getMessage().equals(testError.getMessage());
        }
    }

    @Test
    void mergeAll_withCollector_allValid() {
        Validation<TestError, String> v1 = Validation.valid("a");
        Validation<TestError, String> v2 = Validation.valid("b");
        Validation<TestError, String> v3 = Validation.valid("c");

        Validation<TestError, List<String>> result = ValidationUtils.mergeAll(
            Collectors.toList(), v1, v2, v3);

        assertTrue(result.isValid());
        assertEquals(List.of("a", "b", "c"), result.get().orElse(null));
    }

    @Test
    void mergeAll_withCollector_someInvalid() {
        Validation<TestError, String> v1 = Validation.valid("a");
        Validation<TestError, String> v2 = Validation.invalid(new TestError("error1"));
        Validation<TestError, String> v3 = Validation.invalid(new TestError("error2"));

        Validation<TestError, List<String>> result = ValidationUtils.mergeAll(
            Collectors.toList(), v1, v2, v3);

        assertTrue(result.isInvalid());
        assertEquals("error1, error2", result.error().orElse(null).getMessage());
    }

    @Test
    void mergeAll_withCollector_allInvalid() {
        Validation<TestError, String> v1 = Validation.invalid(new TestError("error1"));
        Validation<TestError, String> v2 = Validation.invalid(new TestError("error2"));

        Validation<TestError, List<String>> result = ValidationUtils.mergeAll(
            Collectors.toList(), v1, v2);

        assertTrue(result.isInvalid());
        assertEquals("error1, error2", result.error().orElse(null).getMessage());
    }

    @Test
    void mergeAll_withMap_allValid() {
        Map<String, Validation<TestError, ?>> validations = new HashMap<>();
        validations.put("key1", Validation.valid("value1"));
        validations.put("key2", Validation.valid(42));

        Validation<TestError, Map<String, Object>> result = ValidationUtils.mergeAll(validations);

        assertTrue(result.isValid());
        Map<String, Object> expected = Map.of("key1", "value1", "key2", 42);
        assertEquals(expected, result.get().orElse(null));
    }

    @Test
    void mergeAll_withMap_someInvalid() {
        Map<String, Validation<TestError, ?>> validations = new HashMap<>();
        validations.put("key1", Validation.valid("value1"));
        validations.put("key2", Validation.invalid(new TestError("error1")));
        validations.put("key3", Validation.invalid(new TestError("error2")));

        Validation<TestError, Map<String, Object>> result = ValidationUtils.mergeAll(validations);

        assertTrue(result.isInvalid());
        assertEquals("error1, error2", result.error().orElse(null).getMessage());
    }

    @Test
    void valid_returnsValidValidation() {
        String value = "test";
        Validation<TestError, String> result = ValidationUtils.valid(value);

        assertTrue(result.isValid());
        assertEquals(value, result.get().orElse(null));
    }
}