package org.dotspace.oofp.support.tokenizer;

import java.util.List;

import org.dotspace.oofp.support.FixedLengthTokenizer;
import org.dotspace.oofp.support.FixedLengthTokenizers;
import org.dotspace.oofp.support.builder.GeneralBuilder;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FixedLengthTokenizationActions<T> {

	private Class<T> destinationClazz;

	private List<FixedLengthTokenizationAction> actions;

	@JsonCreator
	public static <T> FixedLengthTokenizationActions<T> into(
			@JsonProperty("destinationClazz") Class<T> destinationClazz,
			@JsonProperty("actions") List<FixedLengthTokenizationAction> actions) {
		return new FixedLengthTokenizationActions<>(destinationClazz, actions);
	}
	
	public FixedLengthTokenizationActions(Class<T> destinationClazz,
			List<FixedLengthTokenizationAction> actions) {
		this.destinationClazz = destinationClazz;
		this.actions = actions;
	}
	
	public Class<T> getDestinationClazz() {
		return destinationClazz;
	}

	public FixedLengthTokenizationActions<T> parse(
			String fieldPath, int textLength, String valueMapperExpression) {
		FixedLengthTokenizationAction action = parseField(
				fieldPath, textLength, valueMapperExpression).build();
		actions.add(action);
		return this;
	}

	private GeneralBuilder<Object, FixedLengthTokenizationAction> parseField(
			String fieldPath, int textLength, String valueMapperExpression) {
		return GeneralBuilders.of(FixedLengthTokenizationAction::new)
				.with(GeneralBuildingWriters.set(
						FixedLengthTokenizationAction::setPropertyPath, fieldPath))
				.with(GeneralBuildingWriters.set(
						FixedLengthTokenizationAction::setTextLength, textLength))
				.with(GeneralBuildingWriters.set(
						FixedLengthTokenizationAction::setValueMapperExpression, 
						valueMapperExpression));
	}

	public FixedLengthTokenizationActions<T> parse(String fieldPath, 
			int textLength, String valueMapperExpression, 
			String optionsExpression) {
		FixedLengthTokenizationAction action = 
				parseField(fieldPath, textLength, valueMapperExpression)
				.with(GeneralBuildingWriters.set(
						FixedLengthTokenizationAction::setOptionsExpression, 
						optionsExpression))
				.build();
		actions.add(action);
		return this;
	}

	public TokenizationResult<T> tokenize(
			FixedLengthTokenizers fixedLengthTokenizers, String msgText) {
		FixedLengthTokenizer<?, T> tokenizer = fixedLengthTokenizers
				.tokenize(() -> newDestination())
				.addAll(actions);
		
		TokenizationResult<T> result = tokenizer.split(msgText);
		return result;
	}

	private T newDestination() {
		try {
			return destinationClazz.newInstance();
		} catch (Throwable e) {
			return null;
		}
	}

}
