package org.dotspace.oofp.support.transform;

import java.io.Serializable;

public class TransformMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5122375620208944347L;

	private Integer mapperItemSequence;
	
	private String sourceExpression;
	
	private String predicateName;
	
	private String predicateOptions;
	
	private String valueMapperName;
	
	private String valueMapperOptions;
	
	private String indicator;
	
	private String destinationExpression;
	
	private String collectorName;
	
	private String collectorOptions;

	public Integer getMapperItemSequence() {
		return mapperItemSequence;
	}

	public void setMapperItemSequence(Integer mapperItemSequence) {
		this.mapperItemSequence = mapperItemSequence;
	}

	public String getPredicateName() {
		return predicateName;
	}

	public void setPredicateName(String predicateName) {
		this.predicateName = predicateName;
	}

	public String getPredicateOptions() {
		return predicateOptions;
	}

	public void setPredicateOptions(String predicateOptions) {
		this.predicateOptions = predicateOptions;
	}

	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public String getSourceExpression() {
		return sourceExpression;
	}

	public void setSourceExpression(String sourceExpression) {
		this.sourceExpression = sourceExpression;
	}

	public String getValueMapperName() {
		return valueMapperName;
	}

	public void setValueMapperName(String valueMapperName) {
		this.valueMapperName = valueMapperName;
	}

	public String getValueMapperOptions() {
		return valueMapperOptions;
	}

	public void setValueMapperOptions(String valueMapperOptions) {
		this.valueMapperOptions = valueMapperOptions;
	}

	public String getDestinationExpression() {
		return destinationExpression;
	}

	public void setDestinationExpression(String destinationExpression) {
		this.destinationExpression = destinationExpression;
	}

	public String getCollectorName() {
		return collectorName;
	}

	public void setCollectorName(String collectorName) {
		this.collectorName = collectorName;
	}

	public String getCollectorOptions() {
		return collectorOptions;
	}

	public void setCollectorOptions(String collectorOptions) {
		this.collectorOptions = collectorOptions;
	}

}
