package org.dotspace.oofp.utils.violation;

import lombok.experimental.UtilityClass;
import org.dotspace.oofp.utils.violation.joinable.Violations;

import java.util.List;
import java.util.function.Function;

@UtilityClass
public class ViolationFilters {

    /**
     * keepAll：保留所有 violations（不過濾）
     */
    public static Function<Violations, Violations> keepAll() {
        return Function.identity();
    }

    /**
     * dropAll：丟棄所有 violations（總是視為無錯誤）
     */
    public static Function<Violations, Violations> dropAll() {
        return violations -> Violations.from(List.of());
    }

    /**
     * onlySevere：只保留「嚴重」錯誤
     * （需要 GeneralViolation 類別支援 isSevere() 判斷）
     */
    public static Function<Violations, Violations> onlySevere() {
        return violations -> Violations.from(
                violations.stream()
                        .filter(GeneralViolation::isSevere)
                        .toList()
        );
    }

    /**
     * ignoreWarnings：忽略「警告型」錯誤，只保留非警告
     * （需要 GeneralViolation 類別支援 isWarning() 判斷）
     */
    public static Function<Violations, Violations> ignoreWarnings() {
        return violations -> Violations.from(
                violations.stream()
                        .filter(vio -> !vio.isWarning())
                        .toList()
        );
    }

}
