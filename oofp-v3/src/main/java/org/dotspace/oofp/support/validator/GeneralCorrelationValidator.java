package org.dotspace.oofp.support.validator;

import org.dotspace.oofp.model.dto.correlation.AnnotatedFieldSelection;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.validator.constraint.MandatoryField;
import org.dotspace.oofp.support.validator.constraint.MandatoryFieldCase;
import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import org.dotspace.oofp.utils.functional.Functions;

import org.dotspace.oofp.utils.functional.Predicates;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GeneralCorrelationValidator<I> {

    private final ExpressionEvaluations expressionEvaluations;

    private Map<String, List<String>> invalidFieldMessages;

    public boolean validate(I info) {

        List<Field> fields = new ArrayList<>();
        ReflectionUtils.doWithFields(info.getClass(), fields::add);

        Map<String, AnnotatedFieldSelection<MandatoryField>> fieldSelections = fields
                .stream()
                .map(this::createMandatoryFieldSelection)
                .filter(this::isAnnotationNotPresent)
                .collect(Collectors.toMap(AnnotatedFieldSelection::getName,
                        Functions.self()));

        invalidFieldMessages = new HashMap<>();

        fieldSelections.forEach((name, fieldSelection) -> {
            MandatoryField mandatoryField = fieldSelection.getAnnotation();
            MandatoryFieldCase[] cases = mandatoryField.cases();

            for (MandatoryFieldCase mfCase : cases) {
                putInvalidFieldMessage(info, name, fieldSelection, mfCase);
            }
        });

        return invalidFieldMessages.isEmpty();

    }

    private void putInvalidFieldMessage(
            I info, String name, AnnotatedFieldSelection<MandatoryField> fieldSelection, MandatoryFieldCase mfCase) {
        if (Optional.ofNullable(mfCase.when())
                .map(expressionEvaluations::evaluate)
                .map(eval -> eval.<Boolean>getValue(info))
                .orElse(false)) {
            Field field = fieldSelection.getField();
            Object value = Optional.ofNullable(expressionEvaluations
                            .evaluate(field.getName()))
                    .map(ev -> ev.getValue(info))
                    .orElse(null);

            String propertyNameMessage = String.format("property name = %s", name);
            if (!isPresent(value)) {
                if (mfCase.present()) {
                    invalidFieldMessages.put(name, Arrays.asList(
                            "此欄位必須輸入", propertyNameMessage));
                }
                return;
            }

            if (mfCase.empty()) {
                invalidFieldMessages.put(name, Arrays.asList(
                        "此欄位不可輸入", propertyNameMessage));
                return;
            }

            Optional.ofNullable(mfCase.valueTest())
                    .filter(Predicates.not(String::isEmpty))
                    .ifPresent(valueTest -> {

                        String actualValueTest = valueTest.replace("$$", name);

                        if (!Optional.of(actualValueTest)
                                .map(expressionEvaluations::evaluate)
                                .map(eval -> eval
                                        .<Boolean>getValue(info))
                                .orElse(false)) {

                            invalidFieldMessages.put(name, Arrays.asList(
                                    "欄位內容檢核失敗",
                                    propertyNameMessage));
                        }
                    });
        }
    }

    private boolean isAnnotationNotPresent(AnnotatedFieldSelection<MandatoryField> e) {
        return null != e.getAnnotation();
    }

    private AnnotatedFieldSelection<MandatoryField> createMandatoryFieldSelection(Field fld) {
        return GeneralBuilders
                .supply(AnnotatedFieldSelection<MandatoryField>::new)
                .with(WriteOperations.set(
                        AnnotatedFieldSelection<MandatoryField>::setName,
                        fld.getName()))
                .with(WriteOperations.set(
                        AnnotatedFieldSelection<MandatoryField>::setField,
                        fld))
                .with(WriteOperations.set(
                        AnnotatedFieldSelection<MandatoryField>::setAnnotation,
                        fld.getAnnotation(MandatoryField.class)))
                .build();
    }

    private boolean isPresent(Object object) {
        if (null == object) {
            return false;
        }

        return Optional.ofNullable(object.toString())
                .filter(Predicates.not(String::isEmpty))
                .isPresent();
    }

}
