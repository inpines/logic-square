package org.dotspace.oofp.support.conditional;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.dotspace.oofp.util.TypeConversions;
import org.dotspace.oofp.util.functional.Casters;
import org.dotspace.oofp.util.functional.FunctionalSupport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Conditionalities {
			
	private ObjectMapper objectMapper;
	
	private FunctionalSupport functionalSupport;
	
	public <R> ConditionalContext<R> of(Supplier<R> supplier) {
		return new ConditionalContext<R>(supplier, functionalSupport);
	}
	
	public <R> ConditionalContext<R> of(Supplier<R> supplier,
			String predicate) {
		return new ConditionalContext<R>(supplier, predicate, 
				functionalSupport);
	}
	
	public <R> ConditionalContext<R> of(Supplier<R> supplier, 
			List<String> predicates) {
		return new ConditionalContext<R>(supplier, predicates, 
				functionalSupport);
	}

	public <R> ConditionalContext<R> load(Supplier<R> supplier,
			File file) throws Throwable {
		
		Map<String, Object> hash = objectMapper.readValue(
				file, new TypeReference<Map<String, Object>>() {});
		
		List<String> predicates = Optional
				.ofNullable(hash.get("predicates"))
				.map(Casters.forList(String.class))
				.orElse(Collections.emptyList());
		
		ConditionalContext<R> result = predicates.isEmpty() ? of(supplier) :
			of(supplier, predicates);

		List<Map<String, Object>> conditionalItems = Optional
				.ofNullable(hash.get("conditionalItems"))
				.map(Casters.forListOfResultMap())
				.orElse(Collections.emptyList());
		
		conditionalItems.forEach(item -> {
			result.withCondition(TypeConversions.convertToClazz(
					ConditionalItem.class, item));
		});
		
		return result;
	}
	
	public FunctionalSupport getFunctionalSupport() {
		return functionalSupport;
	}

	public void setFunctionalSupport(FunctionalSupport functionalSupport) {
		this.functionalSupport = functionalSupport;
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

}
