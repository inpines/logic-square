package org.dotspace.oofp.support.validator.constraint;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dotspace.oofp.model.dto.correlation.AnnotatedFieldSelection;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;

import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import org.dotspace.oofp.utils.functional.Functions;

import org.dotspace.oofp.utils.functional.Predicates;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ReflectionUtils;

@RequiredArgsConstructor
public class CorrelationValidator implements ConstraintValidator<Correlation, Object> {

	private final ExpressionEvaluations expressionEvaluations;

	@Override
	public boolean isValid(Object target, 
			ConstraintValidatorContext context) {

		List<Field> fields = new ArrayList<>();
		ReflectionUtils.doWithFields(target.getClass(), fields::add);
		Map<String, AnnotatedFieldSelection<MandatoryField>> fieldSelections = fields.stream()
				.map(fld -> GeneralBuilders
						.supply(AnnotatedFieldSelection<MandatoryField>::new)
						.with(WriteOperations.set(
								AnnotatedFieldSelection<MandatoryField>::setName, fld.getName()))
						.with(WriteOperations.set(
								AnnotatedFieldSelection<MandatoryField>::setField, fld))
						.with(WriteOperations.set(
								AnnotatedFieldSelection<MandatoryField>::setAnnotation, 
								fld.getAnnotation(MandatoryField.class)))
						.build())
				.filter(e -> null != e.getAnnotation())
				.collect(Collectors.toMap(
						AnnotatedFieldSelection::getName, Functions.self()));
				
		List<String> invalidFieldNames = new ArrayList<>();
		
		fieldSelections.forEach((name, fieldSelection) -> {
			MandatoryField mandatoryField = fieldSelection.getAnnotation();
			MandatoryFieldCase[] cases = mandatoryField.cases();

            for (MandatoryFieldCase mfCase : cases) {
				eachMfCase(target, context, name, fieldSelection, mfCase, invalidFieldNames);
			}
		});
		
		return invalidFieldNames.isEmpty();
	}

	private void eachMfCase(
			Object target, ConstraintValidatorContext context, String name,
			AnnotatedFieldSelection<MandatoryField> fieldSelection, MandatoryFieldCase mfCase,
			List<String> invalidFieldNames) {
		if (isPropPresent(target, MandatoryFieldCase::when, mfCase)) {
			Field field = fieldSelection.getField();
			Object value = Optional.ofNullable(expressionEvaluations
							.evaluate(field.getName()))
					.map(ev -> ev.getValue(target))
					.orElse(null);

			if (!isPresent(value)) {
				if (mfCase.present()) {
					context.buildConstraintViolationWithTemplate(
									"此欄位必須輸入")
							.addPropertyNode(name)
							.addConstraintViolation();
					invalidFieldNames.add(name);
				}
				return;
			}

			if (mfCase.empty()) {
				context.buildConstraintViolationWithTemplate(
								"此欄位不可輸入")
						.addPropertyNode(name)
						.addConstraintViolation();
				invalidFieldNames.add(name);
				return;
			}

			Optional.ofNullable(mfCase.valueTest())
					.filter(Predicates.not(String::isEmpty))
					.ifPresent(valueTest -> {

						String actualValueTest = valueTest.replace("$$", name);

						Maybe.given(target).filter(
								t -> !isPropPresent(t, x -> actualValueTest, null))
								.match(t -> {
									context.buildConstraintViolationWithTemplate(
													"欄位內容檢核失敗")
											.addPropertyNode(name)
											.addConstraintViolation();
									invalidFieldNames.add(name);

								});
					});
		}
	}

	private <T> boolean isPropPresent(
			Object target, Function<T, String> reader,
			T props) {
		return Optional.ofNullable(props)
				.map(reader)
				.map(expressionEvaluations::evaluate)
				.filter(eval -> Maybe.given(eval.<Boolean>getValue(target))
						.map(Boolean::booleanValue)
						.orElse(false))
				.isPresent();
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
