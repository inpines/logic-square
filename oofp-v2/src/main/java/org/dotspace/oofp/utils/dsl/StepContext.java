package org.dotspace.oofp.utils.dsl;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.violation.GeneralViolation;
import org.dotspace.oofp.utils.violation.ViolationSeverity;
import org.dotspace.oofp.utils.violation.joinable.Violations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Getter
@Builder(setterPrefix = "with")
public class StepContext<T> {

    private T payload; // 核心資料（主資料）

    private Violations violations; // 收集錯誤

    @Getter(lombok.AccessLevel.NONE)
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

    public StepContext<T> addViolation(GeneralViolation violation) {
        return StepContext.<T>builder()
                .withPayload(payload)
                .withViolations(violations.join(Violations.from(List.of(violation))))
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

    public <R> R getAttribute(String name, Function<Object, R> applier) {
        return Maybe.given(attributes.get(name))
                .map(applier)
                .orElse(null);
    }


    // 設定屬性值
    public StepContext<T> withAttribute(String key, Object value) {
        attributes.put(key, value);
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

}
