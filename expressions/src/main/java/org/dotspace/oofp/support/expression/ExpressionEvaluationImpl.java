package org.dotspace.oofp.support.expression;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.dotspace.oofp.support.ExpressionEvaluation;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.util.functional.Casters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class ExpressionEvaluationImpl implements ExpressionEvaluation {

	private ApplicationContext applicationContext;
	
	private Expression expression;
	
	protected ExpressionEvaluationImpl(
			ApplicationContext applicationContext, String expressionText) {
		this.applicationContext = applicationContext;
		
		this.expression = Optional.ofNullable(expressionText)
				.map(expr -> {
					SpelExpressionParser parser = new SpelExpressionParser();
					return parser.parseExpression(expr);
				})
				.orElse(null);
	}

	@Override
	public <T> T getValue(Class<T> resultClazz, Object root) {
		EvaluationContext context = getContext(root, Collections.emptyMap());
		return expression.getValue(context, resultClazz);
	}

	@Override
	public <T> T getValue(Class<T> resultClazz, Map<String, Object> variables, Object root) {
		EvaluationContext context = getContext(root, variables);
		return expression.getValue(context, resultClazz);
	}

	@Override
	public <T> T getValueWithVariables(Map<String, Object> variables, Object root) {
		EvaluationContext context = getContext(root, variables);
		return evaluateValue(context);
	}

	private EvaluationContext getContext(Object root, Map<String, Object> variables) {
		EvaluationContext context = GeneralBuilders.of(x -> new StandardEvaluationContext(x))
				.with(GeneralBuildingWriters.set(
						StandardEvaluationContext::setBeanResolver, 
						new BeanFactoryResolver(applicationContext)))
				.build(root);
		variables.forEach((name, value) -> context.setVariable(name, value));
		return context;
	}

	@Override
	public <T> T getValue(Object root) {
		EvaluationContext context = getContext(root, Collections.emptyMap());
		return evaluateValue(context);
	}

	private <T> T evaluateValue(EvaluationContext context) {

		T value = Optional.ofNullable(expression)
				.map(e -> context != null ? e.getValue(context) : e.getValue())
				.map(Casters.<T>cast())
				.orElse(null);
		
		return value;
	}

	@Override
	public <T> T getValue() {
		T value = evaluateValue(null);
		
		return value;
	}

	@Override
	public <T> void setValue(Map<String, Object> variables, T root, Object value) {
		EvaluationContext context = getContext(root, variables);
		expression.setValue(context, value);
	}

	@Override
	public <T> void setValue(T root, Object value) {
		expression.setValue(root, value);
	}

}
