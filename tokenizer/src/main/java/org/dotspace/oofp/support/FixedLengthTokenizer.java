package org.dotspace.oofp.support;

import java.util.List;
import java.util.function.Function;

import org.dotspace.oofp.support.tokenizer.FixedLengthTokenizationAction;
import org.dotspace.oofp.support.tokenizer.TokenizationResult;

public interface FixedLengthTokenizer<A, T> {

	public FixedLengthTokenizer<A, T> add(String propertyPath, 
			Function<String, Object> valueMapper, Integer length);

	public FixedLengthTokenizer<A, T> add(String propertyPath, 
			String tokenizationMapperName, Function<String, Object> valueMapper, 
			Integer length);
	
	public FixedLengthTokenizer<A, T> addAll(List<FixedLengthTokenizationAction> tokenizationActions);

	public TokenizationResult<T> split(String msgText);
	
	public Long getTotalSize();

}