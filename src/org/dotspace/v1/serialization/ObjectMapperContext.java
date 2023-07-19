package org.dotspace.v1.serialization;

import org.dotspace.v1.creation.CreationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperContext {

	public CreationBuilder<ObjectMapper> get();
	
}
