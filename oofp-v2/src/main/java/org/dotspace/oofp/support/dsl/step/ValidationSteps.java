package org.dotspace.oofp.support.dsl.step;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.validator.constraint.MandatoryField;
import org.dotspace.oofp.support.validator.constraint.MandatoryFieldCase;
import org.dotspace.oofp.utils.AnnotatedFieldSelection;
import org.dotspace.oofp.utils.Reflections;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.GeneralBuildingWriters;
import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.dsl.StepContext;
import org.dotspace.oofp.utils.functional.Casters;
import org.dotspace.oofp.utils.functional.Functions;
import org.dotspace.oofp.utils.functional.Predicates;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.Monads;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.GeneralViolation;
import org.dotspace.oofp.utils.violation.joinable.Violations;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ValidationSteps {

    public static final String NAME = "name";
    public static final String REQUIRED = "required";
    public static final String LENGTH = "length";
    public static final String PROPERTY = "property";
    public static final String REGEX = "regex";
    public static final String REGEX_MESSAGE = "regexMessage";
    public static final String PROPERTY_NAME_S = "property name = %s";

    private Validator validator;

    private ExpressionEvaluations expressionEvaluations;

    public <T> BehaviorStep<T> constraintValidator() {
        return stepContext -> Maybe.given(validator.validate(stepContext.getPayload()))
                .filter(viols -> !viols.isEmpty())
                .map(viols -> {
                    Violations violations = Violations.empty();
                    viols.forEach(vio -> violations.join(Violations.of(GeneralBuilders
                            .of(GeneralViolation::new)
                            .with(GeneralBuildingWriters.set(
                                    GeneralViolation::setValidationName,
                                    getPropertyPath(vio)))
                            .with(GeneralBuildingWriters.set(
                                    GeneralViolation::setMessages, getMessages(vio)))
                            .build())));
                    return violations;
                })
                .map(Validation::<Violations, StepContext<T>>invalid)
                .orElse(Validation.valid(stepContext));
    }

    private <T> String getPropertyPath(ConstraintViolation<T> violation) {
        return Optional.ofNullable(violation.getPropertyPath())
                .map(Path::toString)
                .orElse(StringUtils.EMPTY);
    }

    private <T> List<String> getMessages(ConstraintViolation<T> violation) {
        return List.of(Optional.ofNullable(violation.getMessage())
                .orElse(StringUtils.EMPTY));
    }

    public <T> BehaviorStep<T> correlationValidator() {
        return stepContext -> Violations.from(validateCorrelation(stepContext.getPayload()))
                .toValidation(stepContext);
    }

    private <T> List<GeneralViolation> validateCorrelation(T dto) {

        Map<String, AnnotatedFieldSelection<MandatoryField>> fieldSelections =
                getMandatoryFieldSelections(dto);

        Map<String, List<String>> invalidFieldMessages = fieldSelections
                .entrySet().stream()
                .flatMap(e ->
                        validateFieldSelection(dto, e.getKey(), e.getValue())
                                .entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue));

        return invalidFieldMessages.entrySet().stream()
                .map(e -> GeneralBuilders
                        .of(GeneralViolation::new)
                        .with(GeneralBuildingWriters.set(
                                GeneralViolation::setValidationName, e.getKey()))
                        .with(GeneralBuildingWriters.set(
                                GeneralViolation::setMessages, e.getValue()))
                        .build())
                .toList();
    }

    private <T> Map<String, List<String>> validateFieldSelection(
            T dto, String name,
            AnnotatedFieldSelection<MandatoryField> fieldSelection) {
        MandatoryField mandatoryField = fieldSelection.getAnnotation();
        MandatoryFieldCase[] cases = mandatoryField.cases();
        Map<String, List<String>> invalidFieldMessages = new HashMap<>();

        for (MandatoryFieldCase mfCase : cases) {
            eachMfCase(dto, name, fieldSelection, mfCase, invalidFieldMessages);
        }

        return invalidFieldMessages;
    }

    private <T> void eachMfCase(
            T dto, String name, AnnotatedFieldSelection<MandatoryField> fieldSelection, MandatoryFieldCase mfCase,
            Map<String, List<String>> invalidFieldMessages) {
        if (Optional.ofNullable(mfCase.when())
                .map(expressionEvaluations::parse)
                .map(eval -> eval.<Boolean>getValue(dto))
                .orElse(false)) {
            Field field = fieldSelection.getField();
            Object value = Optional.ofNullable(expressionEvaluations.parse(field.getName()))
                    .map(ev -> ev.getValue(dto))
                    .orElse(null);

            if (!isPresent(value)) {
                if (mfCase.present()) {
                    invalidFieldMessages.put(name, Arrays.asList(
                            "此欄位必須輸入",
                            String.format(PROPERTY_NAME_S, name)));
                }
                return;
            }

            if (!mfCase.present()) {

                invalidFieldMessages.put(name, Arrays.asList(
                        "此欄位不可輸入",
                        String.format(PROPERTY_NAME_S, name)));
                return;
            }

            Optional.ofNullable(mfCase.valueTest())
                    .filter(Predicates.not(String::isEmpty))
                    .ifPresent(valueTest -> {

                        String actualValueTest = valueTest.replace("$$", name);

                        if (!Optional.of(actualValueTest)
                                .map(expressionEvaluations::parse)
                                .map(eval -> eval
                                        .<Boolean>getValue(dto))
                                .orElse(false)) {

                            invalidFieldMessages.put(
                                    name, Arrays.asList(
                                            "欄位內容檢核失敗",
                                            String.format(
                                                    PROPERTY_NAME_S,
                                                    name)));
                        }
                    });
        }
    }

    private boolean isPresent(Object object) {
        if (null == object) {
            return false;
        }

        return Optional.ofNullable(object.toString())
                .filter(Predicates.not(String::isEmpty))
                .isPresent();
    }

    private <T> Map<String, AnnotatedFieldSelection<MandatoryField>> getMandatoryFieldSelections(T dto) {
        return Reflections.getFields(dto.getClass()).stream()
                .map(this::createMandatoryFieldSelection)
                .filter(this::isAnnotationNotPresent)
                .collect(Collectors.toMap(AnnotatedFieldSelection::getName,
                        Functions.self()));
    }

    private boolean isAnnotationNotPresent(
            AnnotatedFieldSelection<MandatoryField> e) {
        return null != e.getAnnotation();
    }

    private AnnotatedFieldSelection<MandatoryField> createMandatoryFieldSelection(Field fld) {
        return GeneralBuilders
                .of(AnnotatedFieldSelection<MandatoryField>::new)
                .with(GeneralBuildingWriters.set(
                        AnnotatedFieldSelection<MandatoryField>::setName,
                        fld.getName()))
                .with(GeneralBuildingWriters.set(
                        AnnotatedFieldSelection<MandatoryField>::setField,
                        fld))
                .with(GeneralBuildingWriters.set(
                        AnnotatedFieldSelection<MandatoryField>::setAnnotation,
                        fld.getAnnotation(MandatoryField.class)))
                .build();
    }

    public <T> BehaviorStep<T> propertiesValidator(List<Map<String, Object>> propInfos) {
        return stepContext -> Violations.from(validateProperties(stepContext.getPayload(), propInfos))
                .toValidation(stepContext);
    }

    private <T> List<GeneralViolation> validateProperties(
            T dto, List<Map<String, Object>> propInfos) {

        List<GeneralViolation> violations = new ArrayList<>();

        propInfos.forEach(propInfo -> {
            String name = getString(propInfo, NAME);
            String property = getString(propInfo, PROPERTY);
            int length = getLength(propInfo);
            boolean required = getRequired(propInfo);
            String regex = getString(propInfo, REGEX);
            String regexMessage = getString(propInfo, REGEX_MESSAGE);

            Maybe<?> value = Maybe.given(property)
                    .map(expressionEvaluations::parse)
                    .map(eval -> eval.getValue(dto));

            List<String> errorMessages = new ArrayList<>();
            if (value.isEmpty() && required) {
                errorMessages.add(StringUtils.join(List.of(name, "-不可空白")));
            }

            if (maybeText(value)
                    .filter(s -> s.length() <= length).isEmpty()) {
                errorMessages.add(StringUtils.join(name, "-資料長度不可超過"));
            }

            if (maybeText(value)
                    .filter(s -> s.matches(regex)).isEmpty()) {
                errorMessages.add(StringUtils.join(name, regexMessage));
            }

            violations.add(GeneralBuilders.of(GeneralViolation::new)
                    .with(GeneralBuildingWriters.set(
                            GeneralViolation::setValidationName, property)
                    )
                    .with(GeneralBuildingWriters.set(
                            GeneralViolation::setMessages, errorMessages)
                    )
                    .build()
            );
        });

        return violations;
    }

    private Maybe<String> maybeText(Maybe<?> value) {
        return value.map(Objects::toString);
    }

    private boolean getRequired(Map<String, Object> propInfo) {
        return Monads.maybe(propInfo.get(REQUIRED)).fold(Casters.cast(), () -> false);
    }

    private Integer getLength(Map<String, Object> propInfo) {
        return Monads.maybe(propInfo.get(LENGTH)).fold(Casters.cast(), () -> 0);
    }

    private String getString(Map<String, Object> map, String key) {
        return Monads.maybe(map.get(key)).fold(Casters.forText(), () -> StringUtils.EMPTY);
    }

}
