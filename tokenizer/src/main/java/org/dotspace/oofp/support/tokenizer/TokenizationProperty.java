package org.dotspace.oofp.support.tokenizer;

import java.util.function.Function;

public class TokenizationProperty {

	private String path;
	
	private String tokenizationMapperName;
	
	private Function<String, Object> valueMapper;
	
	private String valueMapperOptionsExpression;
	
	private int length;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTokenizationMapperName() {
		return tokenizationMapperName;
	}

	public void setTokenizationMapperName(String tokenizationMapperName) {
		this.tokenizationMapperName = tokenizationMapperName;
	}
	
	public Function<String, Object> getValueMapper() {
		return valueMapper;
	}

	public void setValueMapper(Function<String, Object> valueMapper) {
		this.valueMapper = valueMapper;
	}

	public String getValueMapperOptionsExpression() {
		return valueMapperOptionsExpression;
	}

	public void setValueMapperOptionsExpression(String valueMapperOptionsExpression) {
		this.valueMapperOptionsExpression = valueMapperOptionsExpression;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
