package org.dotspace.oofp.support.expression;

import org.dotspace.oofp.support.common.ExpressionEvaluation;
import org.dotspace.oofp.support.common.ExpressionEvaluations;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ExpressionEvaluatorsImpl implements ExpressionEvaluations, ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public ExpressionEvaluation parse(String expression) {
		ExpressionEvaluation result = new ExpressionEvaluationImpl(applicationContext, expression);
		
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
