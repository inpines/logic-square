package org.dotspace.oofp.model.dto.behaviorstep;

import org.dotspace.oofp.enumeration.stepcontext.ViolationSeverity;
import org.dotspace.oofp.utils.dsl.Joinable;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import org.dotspace.oofp.utils.functional.Functions;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a collection of violations that can be joined together.
 * Provides methods to create violations, join them, and convert them to validations.
 */
@ToString
@Getter
public class Violations implements Joinable<Violations> {

    private Collection<GeneralViolation> violationCollection = new ArrayList<>();

    private Violations() {
    }

    public static Violations from(Collection<GeneralViolation> violations) {
        Violations result = initialize();
        result.violationCollection.addAll(violations);
        return result;
    }

    /**
     * Creates a violation with the specified validation name and message.
     *
     * @param validationName the name of the validation
     * @param message        the message for the violation
     * @return a Violations instance containing the created violation
     */
    public static Violations violate(String validationName, String message) {
        return Violations.of(GeneralBuilders.supply(GeneralViolation::new)
                .with(WriteOperations.set(GeneralViolation::setValidationName, validationName))
                .with(WriteOperations.set(GeneralViolation::setMessages, List.of(message)))
                .build());
    }

    /**
     * Creates a Violations instance containing the specified GeneralViolation.
     *
     * @param violation the GeneralViolation to include
     * @return a Violations instance containing the specified violation
     */
    public static Violations of(GeneralViolation violation) {
        Violations result = initialize();
        result.violationCollection.add(violation);

        return result;
    }

    private static Violations initialize() {
        Violations result = new Violations();
        result.violationCollection = new ArrayList<>();
        return result;
    }

    public static Violations empty() {
        return initialize();
    }

    @Override
    public Violations join(Violations other) {
        return Violations.from(Maybe.just(violationCollection)
                .fold(Functions.addAll(other.violationCollection.stream()
                        .filter(Predicate.not(violationCollection::contains))
                        .toList()), Collections::emptyList));
    }

    public Stream<GeneralViolation> stream() {
        return violationCollection.stream();
    }

    public boolean isEmpty() {
        return violationCollection.isEmpty();
    }

    public void tagStep(String stepName) {
        this.violationCollection.forEach(v -> v.setStepName(stepName));
    }

    public <T> Validation<Violations, StepContext<T>> toValidation(StepContext<T> stepContext) {
        return violationCollection.isEmpty() ? Validation.valid(stepContext) : Validation.invalid(this);
    }

    public static <T> Validation<Violations, StepContext<T>> getInvalidValidation(String resultName, Throwable e) {
        return Validation.invalid(Violations.of(GeneralBuilders.supply(GeneralViolation::new)
                .with(WriteOperations.set(GeneralViolation::setValidationName, resultName))
                .with(WriteOperations.set(
                        GeneralViolation::setMessages, List.of(Maybe.given(e.getMessage())
                                .orElse("exception message is null"), ExceptionUtils.getStackTrace(e))
                ))
                .build())
        );
    }

    public String getSize() {
        return String.valueOf(violationCollection.size());
    }

    public String collectMessages() {
        return violationCollection.stream()
                .map(vio -> String.format("%s -> %s%s", vio.getValidationName(), vio.getMessages(),
                        Maybe.given(vio.getSeverity())
                                .filter(Predicate.not(ViolationSeverity.UNSPECIFIED::equals))
                                .map(severity -> " (" + severity + ")")
                                .orElse(StringUtils.EMPTY)))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Violations that = (Violations) o;
        return Objects.equals(collectMessages(), that.collectMessages());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(collectMessages());
    }

    public Map<String, String> associateMessage() {
        return violationCollection.stream()
                .collect(Collectors.toMap(GeneralViolation::getValidationName,
                        v -> StringUtils.join(v.getMessages(), "\n"),
                        (l, r) -> StringUtils.join(l, "\n", r), HashMap::new));
    }

    public boolean messagesContains(String keyword) {
        if (StringUtils.isBlank(keyword) || null == violationCollection) {
            return false;
        }

        return violationCollection.stream().anyMatch(
                gv -> gv.getMessages().stream().anyMatch(msg -> msg.contains(keyword))
        );
    }

    public boolean namesAnyMatch(@NonNull Predicate<String> namePredicate) {
        if (null == violationCollection) {
            return false;
        }

        return violationCollection.stream().anyMatch(
                gv -> namePredicate.test(gv.getValidationName())
        );
    }
}
