package org.dotspace.serialization.impl;

import org.dotspace.creation.CreationBuilder;
import org.dotspace.creation.Creations;
import org.dotspace.serialization.ObjectMapperContext;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperContextImpl implements ObjectMapperContext {

	@Override
	public CreationBuilder<ObjectMapper> get() {
		return Creations.construct(ObjectMapper::new);
	}

}
