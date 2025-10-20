package org.dotspace.oofp.utils.violation;

import jakarta.validation.ConstraintViolation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.GeneralBuildingWriters;
import org.dotspace.oofp.utils.functional.Casters;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ToString
@EqualsAndHashCode
@Getter
public class GeneralViolation {

    public static final String SEVERE = "severe";
    public static final String WARNING = "warning";
    public static final String SEVERITY = "severity";
    public static final String ORDER_CONTENT = " (%d) %s";

    @Setter
    private String validationName;

    @Setter
    @Getter
    private String stepName;

    @Setter
    private List<String> messages;

    private final Map<String, Object> options = new HashMap<>();

    public boolean isSevere() {
        return isBooleanOption(SEVERE);
    }

    public boolean isWarning() {
        return isBooleanOption(WARNING);
    }

    private <T> Optional<T> maybeOptions(String name, Function<Object, T> mapper) {
        return Optional.ofNullable(options.get(name))
                .map(mapper);
    }

    private boolean isBooleanOption(String name) {
        return maybeOptions(name, Casters.<Boolean>cast())
                .orElse(false);
    }

    /**
     * Get the severity of the violation.
     * If not specified, returns ViolationSeverity.UNSPECIFIED.
     *
     * @return ViolationSeverity
     */
    public ViolationSeverity getSeverity() {
        return maybeOptions(SEVERITY, Casters.<ViolationSeverity>cast())
                .orElse(ViolationSeverity.UNSPECIFIED);
    }

    public static String fromConstraint(Collection<ConstraintViolation<Object>> violations, Map<String, Object> options) {
        List<GeneralViolation> generalViolation = violations.stream()
                .map(violation -> GeneralBuilders.of(GeneralViolation::new)
                        .with(GeneralBuildingWriters.set(
                            GeneralViolation::setValidationName, violation.getPropertyPath().toString()))
                        .with(GeneralBuildingWriters.set(
                            GeneralViolation::setMessages, List.of(violation.getMessage())))
                        .build())
                .toList();
        return getViolationMessages(generalViolation, options);
    }

    /**
     * get violation messages from a list of violations
     * @param violations List of GeneralViolation
     * @return String containing all violation messages formatted with index and validation name
     */
    public static String getViolationMessages(Collection<GeneralViolation> violations, Map<String, Object> options) {
        if (CollectionUtils.isEmpty(violations)) {
            return StringUtils.EMPTY;
        }
        return IntStream.range(0, violations.size())
                        .mapToObj(i -> {
                            GeneralViolation violation = new ArrayList<>(violations).get(i);
                            Maybe<String> withPropertyPath = Maybe.just(options)
                                    .filter(opts -> Boolean.TRUE.equals(
                                            opts.get("withPropertyPath")))
                                    .map(x -> " {%s}");
                            String msgTemplate = withPropertyPath
                                    .map(x -> StringUtils.join(ORDER_CONTENT, " {%s}"))
                                    .orElse(ORDER_CONTENT);

                            return String.format(msgTemplate,
                                    i + 1,
                                    String.join(", ", violation.getMessages()),
                                    withPropertyPath.map(x -> violation.getValidationName())
                                            .orElse(StringUtils.EMPTY));
                        })
                .collect(Collectors.joining("\n"));
    }
}
