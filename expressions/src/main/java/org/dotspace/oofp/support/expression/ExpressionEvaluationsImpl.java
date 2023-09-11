package org.dotspace.oofp.support.expression;

import org.dotspace.oofp.support.ExpressionEvaluation;
import org.dotspace.oofp.support.ExpressionEvaluations;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ExpressionEvaluationsImpl implements ExpressionEvaluations, ApplicationContextAware {

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
