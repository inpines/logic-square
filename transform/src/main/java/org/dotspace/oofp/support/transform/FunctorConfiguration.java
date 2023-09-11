package org.dotspace.oofp.support.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FunctorConfiguration {

	private String name;
	
	private String options;

	@JsonCreator
	public FunctorConfiguration(@JsonProperty("name") String name, 
			@JsonProperty("options") String options) {
		this.name = name;
		this.options = options;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public String getOptions() {
		return options;
	}

	protected void setOptions(String options) {
		this.options = options;
	}
	
}
