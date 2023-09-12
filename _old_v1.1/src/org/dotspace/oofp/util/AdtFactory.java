package org.dotspace.oofp.util;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdtFactory {

	public static <T> Map<String, Object> getMapFrom(T pojo) {

		ObjectMapper objectMapper = new ObjectMapper();
		
		return objectMapper.convertValue(pojo, new TypeReference<Map<String, Object>>() {});
	}
}
