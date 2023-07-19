package org.dotspace.v1.serialization.impl;

import org.dotspace.v1.creation.CreationBuilder;
import org.dotspace.v1.creation.Creations;
import org.dotspace.v1.serialization.ObjectMapperContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperContextImpl implements ObjectMapperContext {

	@Override
	public CreationBuilder<ObjectMapper> get() {
		return Creations.construct(ObjectMapper::new);
	}

}
