package org.dotspace.oofp.utils.violation.joinable;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.GeneralBuildingWriters;
import org.dotspace.oofp.utils.dsl.StepContext;
import org.dotspace.oofp.utils.functional.Functions;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.GeneralViolation;
import org.dotspace.oofp.utils.violation.Joinable;
import org.dotspace.oofp.utils.violation.ViolationSeverity;

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
@RequiredArgsConstructor(staticName = "from") // Use static factory method for initialization
@NoArgsConstructor(access = AccessLevel.PRIVATE) // Private constructor to enforce use of static factory methods
public class Violations implements Joinable<Violations> {

    @NonNull
    private Collection<GeneralViolation> violationCollection;

    /**
     * Creates a violation with the specified validation name and message.
     *
     * @param validationName the name of the validation
     * @param message        the message for the violation
     * @return a Violations instance containing the created violation
     */
    public static Violations violate(String validationName, String message) {
        return Violations.of(GeneralBuilders.of(GeneralViolation::new)
                .with(GeneralBuildingWriters.set(GeneralViolation::setValidationName, validationName))
                .with(GeneralBuildingWriters.set(GeneralViolation::setMessages, List.of(message)))
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
        return Validation.invalid(Violations.of(GeneralBuilders.of(GeneralViolation::new)
                .with(GeneralBuildingWriters.set(GeneralViolation::setValidationName, resultName))
                .with(GeneralBuildingWriters.set(
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

}
