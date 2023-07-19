package org.dotspace.oofp.support.expression;

import org.dotspace.oofp.support.ExpressionEvaluation;
import org.dotspace.oofp.support.Expressions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ExpressionsImpl implements Expressions, ApplicationContextAware {

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
