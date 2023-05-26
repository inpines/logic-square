package org.dotspace.serialization;

import org.dotspace.creation.CreationBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperContext {

	public CreationBuilder<ObjectMapper> get();
	
}
