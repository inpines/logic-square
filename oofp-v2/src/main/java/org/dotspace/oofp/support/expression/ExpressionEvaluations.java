package org.dotspace.oofp.support.expression;

import lombok.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ExpressionEvaluations implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	public ExpressionEvaluation parse(String expression) {

        return new ExpressionEvaluation(applicationContext, expression);
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
