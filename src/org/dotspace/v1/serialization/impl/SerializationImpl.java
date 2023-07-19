package org.dotspace.v1.serialization.impl;

import java.util.Optional;
import java.util.function.Function;

import org.dotspace.v1.serialization.Serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializationImpl<T> implements Serialization<T> {

	private ObjectMapper objectMapper;
	private T model;
	private Function<T, T> tracer;
	
	public static <T> Serialization<T> create(T model, ObjectMapper objectMapper) {
		return new SerializationImpl<>(model, objectMapper);
	}

	private SerializationImpl(T model, ObjectMapper objectMapper) {
		this.model = model;
		this.objectMapper = objectMapper;
	}

	@Override
	public Serialization<T> trace(Function<T, T> tracer) {
		this.tracer = tracer;
		return this;
	}

	@Override
	public <V> V transform(TypeReference<V> typeRef) {
		
		return Optional.ofNullable(model)
				.map(tracer)
				.map(m -> transform(m, typeRef))
				.orElse(null);
	}

	private <V> V transform(T mdl, TypeReference<V> typeRef) {
		byte[] bytes;
		try {
			bytes = objectMapper.writeValueAsBytes(mdl);
			return objectMapper.readValue(bytes, typeRef);
		} catch (Exception e) {
			return null;
		}
	}
}
