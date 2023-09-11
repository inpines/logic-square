package org.dotspace.oofp.support.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MappingFunctorConfiguration extends FunctorConfiguration {

	private TransformMappingType type;
	
	@JsonCreator
	public MappingFunctorConfiguration(@JsonProperty("type") TransformMappingType type, 
			@JsonProperty("name") String name, @JsonProperty("options") String options) {
		super(name, options);
		this.type = type;
	}

	protected TransformMappingType getType() {
		return type;
	}

	protected void setType(TransformMappingType type) {
		this.type = type;
	}
	
}
