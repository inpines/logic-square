package org.dotspace.oofp.support.expression;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.dotspace.oofp.utils.builder.GeneralBuilders;
import org.dotspace.oofp.utils.builder.operation.WriteOperations;
import org.dotspace.oofp.utils.functional.Casters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionEvaluation {

	private final ApplicationContext applicationContext;
	
	private final Expression expression;
	
	protected ExpressionEvaluation(
			ApplicationContext applicationContext, String expressionText) {
		this.applicationContext = applicationContext;
		
		this.expression = Optional.ofNullable(expressionText)
				.map(expr -> {
					SpelExpressionParser parser = new SpelExpressionParser();
					return parser.parseExpression(expr);
				})
				.orElse(null);
	}

	public <T> T getValue(Class<T> resultClazz, Object root) {
		EvaluationContext context = getContext(root, Collections.emptyMap());
		return expression.getValue(context, resultClazz);
	}

	public <T> T getValue(Class<T> resultClazz, Map<String, Object> variables, Object root) {
		EvaluationContext context = getContext(root, variables);
		return expression.getValue(context, resultClazz);
	}

	public <T> T getValueWithVariables(Map<String, Object> variables, Object root) {
		EvaluationContext context = getContext(root, variables);
		return evaluateValue(context);
	}

	public <T> T getValueWithVariables(Map<String, Object> variables) {
		EvaluationContext context = getContext();
		variables.forEach(context::setVariable);

		return evaluateValue(context);
	}

	private EvaluationContext getContext(Object root, Map<String, Object> variables) {
		EvaluationContext context = GeneralBuilders.apply(this::createStandardEvaluationContext)
				.with(WriteOperations.set(
						StandardEvaluationContext::setBeanResolver, 
						new BeanFactoryResolver(applicationContext)))
				.build(root);
		variables.forEach(context::setVariable);
		return context;
	}

	private StandardEvaluationContext createStandardEvaluationContext(Object x) {
		return new StandardEvaluationContext(x);
	}

	public <T> T getValue(Object root) {
		EvaluationContext context = getContext(root, Collections.emptyMap());
		return evaluateValue(context);
	}

	private <T> T evaluateValue(EvaluationContext context) {

        return Optional.ofNullable(expression)
                .map(e -> context != null ? e.getValue(context) : e.getValue())
                .map(Casters.<T>cast())
                .orElse(null);
	}

	public <T> T getValue() {
		EvaluationContext context = getContext();

        return evaluateValue(context);
	}

	private StandardEvaluationContext getContext() {
		return GeneralBuilders.apply(
						x -> new StandardEvaluationContext())
				.with(WriteOperations.set(
						StandardEvaluationContext::setBeanResolver,
						new BeanFactoryResolver(applicationContext)))
				.build();
	}

	public <T> void setValue(Map<String, Object> variables, T root, Object value) {
		EvaluationContext context = getContext(root, variables);
		expression.setValue(context, value);
	}

	public <T> void setValue(T root, Object value) {
		expression.setValue(root, value);
	}

}
