package org.dotspace.oofp.support.expression;

import org.dotspace.oofp.model.dto.expression.ExpressionOperation;
import org.dotspace.oofp.utils.builder.operation.WriteOperation;
import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class ExpressionEvaluations implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public ExpressionEvaluation evaluate(String expression) {

        return new ExpressionEvaluation(applicationContext, expression);
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public ExpressionOperation op(@NonNull String expression) {
		return new ExpressionOperation(expression, this);
	}

	public <T> Predicate<T> test(
			@NonNull String expression, @NonNull Function<T, Map<String, Object>> varsProvider) {
		return op(expression).test(varsProvider);
	}

	public <T, R> Function<T, R> read(
			@NonNull String expression, @NonNull Function<T, Map<String, Object>> varsProvider) {
		return op(expression).read(varsProvider);
	}

	public <T, V> BiConsumer<T, V> write(
			@NonNull String expression, @NonNull Function<T, Map<String, Object>> varsProvider) {
		return op(expression).write(varsProvider);
	}

	public <T> WriteOperation<T> setFromExpression(
			@NonNull String expression, @NonNull String valueExpression,
			@NonNull Function<T, Map<String, Object>> varsProvider) {
		return op(expression).writeOp(varsProvider, op(valueExpression).read(varsProvider));
	}

	public <T, V> WriteOperation<T> setFrom(
			@NonNull String expression, @NonNull Function<T, V> valueProvider,
			@NonNull Function<T, Map<String, Object>> varsProvider) {
		return op(expression).writeOp(varsProvider, valueProvider);
	}

	public <T, V> WriteOperation<T> setValue(
			@NonNull String expression, @NonNull V value,
			@NonNull Function<T, Map<String, Object>> varsProvider) {
		return op(expression).writeOp(varsProvider, t -> value);
	}

	public <T, V> WriteOperation<T> setValueWithVars(
			@NonNull String expression, @NonNull V value,
			@NonNull BiFunction<T, V, Map<String, Object>> varsProvider) {
		return op(expression).writeOp(t -> varsProvider.apply(t, value), t -> value);
	}

	/** 帶 value predicate（例如 Objects::nonNull / notBlank） */
	public <T, V> WriteOperation<T> setWhen(
			@NonNull Predicate<V> predicate,
			@NonNull String expression,
			@NonNull Function<T, Map<String, Object>> varsProvider,
			V value) {

		BiConsumer<T, V> setter = op(expression).write(varsProvider);

		return WriteOperation.when(predicate, setter, value);
	}

	/** 帶 valueProvider + predicate */
	public <T, V> WriteOperation<T> setWhen(
			@NonNull Predicate<V> predicate,
			@NonNull String expression,
			@NonNull Function<T, Map<String, Object>> varsProvider,
			@NonNull Function<T, V> valueProvider) {

		BiConsumer<T, V> setter = op(expression).write(varsProvider);

		return t -> {
			V v = valueProvider.apply(t);
			if (predicate.test(v)) {
				setter.accept(t, v);
			}
		};
	}

}
