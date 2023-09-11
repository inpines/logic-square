package org.dotspace.oofp.support.transform;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.dotspace.oofp.support.ExpressionEvaluations;
import org.dotspace.oofp.util.functional.FunctionalSupport;
import org.springframework.stereotype.Component;

@Component
public class Transformations {

	private ExpressionEvaluations expressionEvaluations;
	private FunctionalSupport functionalSupport;
	
	public <A, T> TransformationContext<T, A> into(Function<A, T> constructor, A arg) {
		return new TransformationContext<>(constructor, arg, expressionEvaluations, functionalSupport);
	}
	
	public <A, T> TransformationContext<T, A> into(Function<A, T> constructor, A arg, 
			List<TransformAction> actions) {
		return new TransformationContext<>(constructor, arg, expressionEvaluations, functionalSupport, 
				actions);
	}
	
	public <T> TransformationContext<T, ?> into(Supplier<T> constructor) {
		return new TransformationContext<>(constructor, expressionEvaluations, functionalSupport);
	}

	public <T> TransformationContext<T, ?> into(Supplier<T> constructor, 
			List<TransformAction> actions) {
		return new TransformationContext<>(constructor, expressionEvaluations, functionalSupport, 
				actions);
	}
	
	public FunctionalSupport getFunctionalSupport() {
		return functionalSupport;
	}

	public void setFunctionalSupport(FunctionalSupport functionalSupport) {
		this.functionalSupport = functionalSupport;
	}

	public ExpressionEvaluations getExpressionEvaluations() {
		return expressionEvaluations;
	}

	public void setExpressionEvaluations(ExpressionEvaluations expressionEvaluations) {
		this.expressionEvaluations = expressionEvaluations;
	}

}
