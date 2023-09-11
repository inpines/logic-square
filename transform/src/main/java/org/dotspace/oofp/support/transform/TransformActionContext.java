package org.dotspace.oofp.support.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;

public class TransformActionContext {

	private String readingExpression;
		
	private List<MappingFunctorConfiguration> mappingFunctors =
			new ArrayList<>();
		
	protected TransformActionContext(String expression) {
		this.readingExpression = expression;
	}

	public TransformActionContext filter(String name, String options) {
		mappingFunctors.add(new MappingFunctorConfiguration(TransformMappingType.PREDICATE,
				name, options));
		return this;
	}
	
	public TransformActionContext map(String expression, String options) {
		mappingFunctors.add(new MappingFunctorConfiguration(TransformMappingType.FUNCTION, 
				expression, options));
		return this;
	}
	
	public TransformActionContext map(String expression) {
		return map(expression, null);
	}
	
	public TransformAction collect(String writingExpression, String name, String options) {
		FunctorConfiguration exprWithOpts = Optional.ofNullable(name)
				.filter(Objects::nonNull)
				.map(nm -> new FunctorConfiguration(name, options))
				.orElse(null);
		
		TransformAction transformAction = GeneralBuilders.of(TransformAction::new)
				.with(GeneralBuildingWriters.set(TransformAction::setCollector, 
						exprWithOpts).filterByValue(Objects::nonNull))
				.with(GeneralBuildingWriters.set(TransformAction::setMappers, 
						mappingFunctors))
				.with(GeneralBuildingWriters.set(TransformAction::setWriter, writingExpression))
				.build(readingExpression);
		return transformAction;
	}
	
	public TransformAction collect(String writingExpression, String CollectionExpression) {
		return collect(writingExpression, CollectionExpression, null);
	}
	
	public TransformAction write(String writingExpression) {
		return collect(writingExpression, null, null);
	}
	
}
