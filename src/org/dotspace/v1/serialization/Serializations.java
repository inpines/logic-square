package org.dotspace.serialization;

import org.dotspace.creation.Creations;
import org.dotspace.creation.expression.SingularPath;
import org.dotspace.serialization.impl.SerializationImpl;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Serializations {
	
	public static <T> Serialization<T> from(T model) {
		return SerializationImpl.create(model, Creations.construct(ObjectMapper::new)
				.take(SingularPath.<ObjectMapper, Boolean>getRootToSet((om, v) -> om.configure(Feature.IGNORE_UNDEFINED, v))
						.assign(true))
				.build());
		
	}
}
