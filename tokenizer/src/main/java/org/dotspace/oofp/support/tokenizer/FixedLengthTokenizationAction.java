package org.dotspace.oofp.support.tokenizer;

public class FixedLengthTokenizationAction {

	private String propertyPath;
	private String tokenizationMapperName;
	private int textLength;
	private String valueMapperExpression;
	private String optionsExpression;
	
	public int getTextLength() {
		return textLength;
	}

	public void setTextLength(int fieldLength) {
		this.textLength = fieldLength;
	}

	public String getValueMapperExpression() {
		return valueMapperExpression;
	}

	public void setValueMapperExpression(String valueMapperExpression) {
		this.valueMapperExpression = valueMapperExpression;
	}

	public String getOptionsExpression() {
		return optionsExpression;
	}

	public void setOptionsExpression(String optionsExpression) {
		this.optionsExpression = optionsExpression;
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	public String getTokenizationMapperName() {
		return tokenizationMapperName;
	}

	public void setTokenizationMapperName(String tokenizationMapperName) {
		this.tokenizationMapperName = tokenizationMapperName;
	}

}
