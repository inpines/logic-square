package org.dotspace.oofp.util;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TypeConversionSupport {

	private ObjectMapper objectMapper;
	
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T> T convert(TypeReference<T> typeReference, 
			Map<String, Object> propertyValues) {
		
		T result = objectMapper.convertValue(propertyValues, typeReference);
		
		return result;
	}

}
