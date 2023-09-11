package org.dotspace.oofp.support.tokenizer;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.dotspace.oofp.support.ExpressionEvaluations;
import org.dotspace.oofp.support.FixedLengthTokenizer;
import org.dotspace.oofp.support.FixedLengthTokenizers;
import org.springframework.stereotype.Component;

@Component
public class FixedLengthTokenizersImpl implements FixedLengthTokenizers {

	protected ExpressionEvaluations expressions;
	
	protected TokenizationFunctors functors;
	
	protected Map<String, Map<Object, FixedLengthTokenizationActions<?>>> tokenizationActionsMappers;
	
	@Override
	public <T> FixedLengthTokenizer<?, T> tokenize(Supplier<T> supplier) {
		return new FixedLengthTokenizerImpl<>(supplier, this);
	}
	
	@Override
	public <A, T> FixedLengthTokenizer<A, T> tokenize(Function<A, T> constructor, A args) {
		return new FixedLengthTokenizerImpl<>(constructor, args, this);
	}

	public ExpressionEvaluations getExpressions() {
		return expressions;
	}

	public void setExpressions(ExpressionEvaluations expressions) {
		this.expressions = expressions;
	}

	public TokenizationFunctors getFunctors() {
		return functors;
	}

	public void setFunctors(TokenizationFunctors functors) {
		this.functors = functors;
		this.functors.associate(expressions);
	}

	public Map<String, Map<Object, FixedLengthTokenizationActions<?>>> getTokenizationActionsMappers() {
		return tokenizationActionsMappers;
	}

	public void setTokenizationActionsMappers(
			Map<String, Map<Object, FixedLengthTokenizationActions<?>>> tokenizationActionsMappers) {
		this.tokenizationActionsMappers = tokenizationActionsMappers;
	}

}
