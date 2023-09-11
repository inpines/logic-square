package org.dotspace.oofp.support.transform;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransformAction {
		
	private String reader;
	
	private List<MappingFunctorConfiguration> mappers;

	private FunctorConfiguration collector;

	private String writer;
	
	@JsonCreator
	public TransformAction(@JsonProperty("reader") String reader) {
		this.reader = reader;
	}

	public String getReader() {
		return reader;
	}

	public FunctorConfiguration getCollector() {
		return collector;
	}

	public void setMappers(List<MappingFunctorConfiguration> mappers) {
		this.mappers = mappers;
	}

	public void setCollector(FunctorConfiguration expression) {
		this.collector = expression;
	}

	public List<MappingFunctorConfiguration> getMappers() {
		return Optional.ofNullable(mappers)
				.orElse(Collections.emptyList());
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String expression) {
		this.writer = expression;
	}

}
