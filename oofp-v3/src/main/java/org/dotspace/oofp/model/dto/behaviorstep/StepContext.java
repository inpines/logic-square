package org.dotspace.oofp.model.dto.behaviorstep;

import com.fasterxml.jackson.core.type.TypeReference;

import org.dotspace.oofp.enumeration.stepcontext.ViolationSeverity;
import org.dotspace.oofp.utils.eip.AttrKey;
import org.dotspace.oofp.utils.functional.Casters;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Getter
@Builder(setterPrefix = "with")
public class StepContext<T> {

    public static final String ATTRIBUTE_IS_MISSING = "attribute is missing: ";
    public static final String ATTRIBUTE_CAST_FAILED = "attribute cast failed: ";
    public static final String CONCAT_ACTUAL_PROMPT = ", actual=";
    public static final String CONCAT_ERROR_PROMPT = ", error=";

    public static final String STEP_CONTEXT_ATTRIBUTE_CAST_MISSING = "step-context.attribute.cast.missing";
    public static final String STEP_CONTEXT_ATTRIBUTE_CAST_FAILED = "step-context.attribute.cast.failed";
    public static final String STEP_CONTEXT_ATTRIBUTE_NULL = "step-context.attribute.null";
    public static final String STEP_CONTEXT_ATTRIBUTE_TYPE_MISMATCH = "step-context.attribute.type.mismatch";
    private T payload; // 核心資料（主資料）

    private Violations violations; // 收集錯誤

    @Getter(AccessLevel.PROTECTED)
    @Builder.Default
    private final Map<String, Object> attributes = new HashMap<>(); // 彈性附加資料

    @Builder.Default
    @Setter
    private boolean aborted = false; // 是否中止流程

    public StepContext<T> transit(T newPayload) {
        return StepContext.<T>builder()
                .withPayload(newPayload)
                .withViolations(violations)
                .withAttributes(attributes)
                .withAborted(aborted)
                .build();
    }

    public StepContext<T> addViolation(Violations violations) {
        return StepContext.<T>builder()
                .withPayload(payload)
                .withViolations(violations.join(this.violations))
                .withAttributes(attributes)
                .withAborted(aborted)
                .build();
    }

    public Violations withViolation(Violations violations) {
        return this.violations.join(violations);
    }

    public boolean hasFatalErrors() {
        return violations.stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.FATAL);
    }

    public boolean hasSevereThan(ViolationSeverity level) {
        return violations.stream()
                .anyMatch(v -> v.getSeverity().ordinal() >= level.ordinal());
    }

    public <R> R getAttributeOrDefault(String name, Function<Object, R> applier, R defaultValue) {
        return getAttribute(name, applier)
                .orElse(defaultValue);
    }

    public <R> Maybe<R> getAttribute(String name, Function<Object, R> applier) {
        return getAttribute(name)
                .map(applier);
    }

    public <R> Maybe<R> getAttribute(String name) {
        return getAttributeRaw(name)
                .map(Casters.cast());
    }

    public <R> Maybe<R> findAttribute(AttrKey<R> attrKey) {
        return getAttributeRaw(attrKey.name())
                .map(Casters.cast(attrKey.typeRef()));
    }

    public <R> Maybe<R> getAttribute(String name, Class<R> clazz) {
        return getAttributeRaw(name)
                .map(Casters.cast(clazz));
    }

    private Maybe<Object> getAttributeRaw(String name) {
        return Maybe.given(attributes.get(name));
    }

    // ---------------------------
    // requireAttr: core
    // ---------------------------

    public <R> Validation<Violations, R> requireAttr(String name, Type type) {
        return getAttributeRaw(name)
                .toValidation(Violations.violate(STEP_CONTEXT_ATTRIBUTE_CAST_MISSING,
                        ATTRIBUTE_IS_MISSING + name))
                .flatMap(raw -> castOrInvalid(raw, type, name));
    }

    public <R> Validation<Violations, R> requireAttr(String name, Class<R> clazz) {
        return getAttributeRaw(name)
                .toValidation(Violations.violate(STEP_CONTEXT_ATTRIBUTE_CAST_MISSING,
                        ATTRIBUTE_IS_MISSING + name))
                .flatMap(raw -> castOrInvalid(raw, clazz, name));
    }

    public <R> Validation<Violations, R> requireAttr(String name, TypeReference<R> typeRef) {
        return getAttributeRaw(name)
                .toValidation(Violations.violate(STEP_CONTEXT_ATTRIBUTE_CAST_MISSING,
                        ATTRIBUTE_IS_MISSING + name))
                .flatMap(raw -> castOrInvalid(raw, typeRef, name));
    }

    // ---------------------------
    // optional helpers
    // ---------------------------

    /** 需要存在且不可為 null（通常 attributes 不會存 null，但保險起見） */
    public <R> Validation<Violations, R> requireAttrNonNull(String name, Type type) {
        return this.<R>requireAttr(name, type)
                .flatMap(v -> Maybe.given(v)
                        .toValidation(Violations.violate(STEP_CONTEXT_ATTRIBUTE_NULL,
                                "attribute is null: " + name)));
    }

    public <R> Validation<Violations, R> requireAttrNonNull(String name, Class<R> clazz) {
        return requireAttr(name, clazz)
                .flatMap(v -> Maybe.given(v)
                        .toValidation(Violations.violate(STEP_CONTEXT_ATTRIBUTE_NULL,
                                "attribute is null: " + name)));
    }

    /** 取不到就用 default（這裡不會 invalid） */
    public <R> R requireAttrOrDefault(String name, Class<R> clazz, R defaultValue) {
        return getAttributeRaw(name)
                .map(raw -> {
                    try {
                        return Casters.cast(clazz).apply(raw);
                    } catch (RuntimeException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    // ---------------------------
    // internal cast helpers
    // ---------------------------

    private static <R> Validation<Violations, R> castOrInvalid(
            Object raw, Type type, String name) {
        try {
            R value = Casters.<R>cast(type).apply(raw);
            return Validation.valid(value);
        } catch (RuntimeException e) {
            return Validation.invalid(Violations.violate(STEP_CONTEXT_ATTRIBUTE_CAST_FAILED,
                    ATTRIBUTE_CAST_FAILED + name + " -> " + type.getTypeName()
                            + CONCAT_ACTUAL_PROMPT + raw.getClass().getName()
                            + CONCAT_ERROR_PROMPT + e.getClass().getSimpleName()));
        }
    }

    private static <R> Validation<Violations, R> castOrInvalid(
            Object raw, Class<R> clazz, String name) {
        try {
            R value = Casters.cast(clazz).apply(raw);
            if (value == null) {
                return Validation.invalid(Violations.violate(STEP_CONTEXT_ATTRIBUTE_TYPE_MISMATCH,
                        "attribute type mismatch: " + name + " expected=" + clazz.getName()
                                + CONCAT_ACTUAL_PROMPT + raw.getClass().getName()));
            }
            return Validation.valid(value);
        } catch (RuntimeException e) {
            return Validation.invalid(Violations.violate(STEP_CONTEXT_ATTRIBUTE_CAST_FAILED,
                    ATTRIBUTE_CAST_FAILED + name + " -> " + clazz.getName()
                            + CONCAT_ACTUAL_PROMPT + raw.getClass().getName()
                            + CONCAT_ERROR_PROMPT + e.getClass().getSimpleName()));
        }
    }

    private static <R> Validation<Violations, R> castOrInvalid(
            Object raw, TypeReference<R> typeRef, String name) {
        try {
            R value = Casters.cast(typeRef).apply(raw);
            return Validation.valid(value);
        } catch (RuntimeException e) {
            return Validation.invalid(Violations.violate(STEP_CONTEXT_ATTRIBUTE_CAST_FAILED,
                    ATTRIBUTE_CAST_FAILED + name + " -> " + typeRef.getType().getTypeName()
                            + CONCAT_ACTUAL_PROMPT + raw.getClass().getName()
                            + CONCAT_ERROR_PROMPT + e.getClass().getSimpleName()));
        }
    }

    // 設定屬性值
    public StepContext<T> withAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public StepContext<T> withNoneAttribute(String key) {
        attributes.remove(key);
        return this;
    }

    // 設定多個屬性值
    public StepContext<T> withAttributes(Map<String, Object> additional) {
        Map<String, Object> merged = new HashMap<>(attributes);
        merged.putAll(additional);
        return StepContext.<T>builder()
                .withPayload(payload)
                .withViolations(violations)
                .withAttributes(merged)
                .withAborted(aborted)
                .build();
    }

    public StepContext<T> mergeViolations(Violations additional) {
        return StepContext.<T>builder()
                .withPayload(payload)
                .withViolations(this.violations.join(additional))
                .withAttributes(attributes)
                .withAborted(aborted)
                .build();
    }

    public <R> StepContext<T> withAttribute(AttrKey<R> attrKey, R attrValue) {
        return withAttribute(attrKey.name(), attrValue);
    }
}
