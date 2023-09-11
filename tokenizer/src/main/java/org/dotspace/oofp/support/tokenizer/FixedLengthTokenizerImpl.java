package org.dotspace.oofp.support.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.FixedLengthTokenizer;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.util.functional.BiConsumers;
import org.dotspace.oofp.util.functional.Suppliers;

public class FixedLengthTokenizerImpl<A, T> implements FixedLengthTokenizer<A, T> {

	private FixedLengthTokenizersImpl fixedLengthTokenizers;
	
	private Function<A, T> constructors;
	private A constructionArgs;
	private List<TokenizationProperty> tokenizationProperties = new ArrayList<>();
	
	protected FixedLengthTokenizerImpl(Supplier<T> supplier, FixedLengthTokenizersImpl fixedLengthTokenizers) {
		super();
		this.constructors = a -> supplier.get();
		this.constructionArgs = null;
		this.fixedLengthTokenizers = fixedLengthTokenizers;
	}
	
	protected FixedLengthTokenizerImpl(Function<A, T> constructor, A constructionArgs, 
			FixedLengthTokenizersImpl fixedLengthTokenizers) {
		super();
		this.constructors = constructor;
		this.constructionArgs = constructionArgs;
		this.fixedLengthTokenizers = fixedLengthTokenizers;
	}
	
	@Override
	public FixedLengthTokenizer<A, T> add(String propertyPath, 
			Function<String, Object> valueMapper, Integer length) {
		tokenizationProperties.add(
				GeneralBuilders.of(TokenizationProperty::new)
				.with(GeneralBuildingWriters.set(
						TokenizationProperty::setPath, propertyPath))
				.with(GeneralBuildingWriters.set(
						TokenizationProperty::setValueMapper, valueMapper))
				.with(GeneralBuildingWriters.set(
						TokenizationProperty::setLength, length))
				.build());
		return this;
	}
	
	@Override
	public FixedLengthTokenizer<A, T> add(String propertyPath, 
			String tokenizationMapperName, Function<String, Object> valueMapper, 
			Integer length) {
		tokenizationProperties.add(
				GeneralBuilders.of(TokenizationProperty::new)
				.with(GeneralBuildingWriters.set(
						TokenizationProperty::setPath, propertyPath))
				.with(GeneralBuildingWriters.set(
						TokenizationProperty::setTokenizationMapperName, 
						tokenizationMapperName))
				.with(GeneralBuildingWriters.set(
						TokenizationProperty::setValueMapper, valueMapper))
				.with(GeneralBuildingWriters.set(
						TokenizationProperty::setLength, length))
				.build());
		return this;
	}

	@Override
	public FixedLengthTokenizer<A, T> addAll(
			List<FixedLengthTokenizationAction> tokenizationActions) {
		tokenizationActions.forEach(action -> {
			String propertyPath = action.getPropertyPath();
			String tokenizationMapperName = action.getTokenizationMapperName();
			int length = action.getTextLength();
			Function<String, Object> valueMapper = getValueMapper(action);
			tokenizationProperties.add(GeneralBuilders.of(TokenizationProperty::new)
					.with(GeneralBuildingWriters.set(
							TokenizationProperty::setPath, propertyPath))
					.with(GeneralBuildingWriters.set(
							TokenizationProperty::setTokenizationMapperName, 
							tokenizationMapperName))
					.with(GeneralBuildingWriters.set(
							TokenizationProperty::setValueMapper, valueMapper))
					.with(GeneralBuildingWriters.set(
							TokenizationProperty::setValueMapperOptionsExpression, 
							action.getOptionsExpression()))
					.with(GeneralBuildingWriters.set(
							TokenizationProperty::setLength, length))
					.build());
		});
		return this;
	}
	
	private Function<String, Object> getValueMapper(FixedLengthTokenizationAction action) {
		String optionsExpression = action.getOptionsExpression();
		
		Object options = Optional.ofNullable(optionsExpression)
				.map(optsExpr -> fixedLengthTokenizers.expressions.parse(optsExpr)
						.getValue(fixedLengthTokenizers.functors))
				.orElse(null);
		
		String valueMapperExpression = action.getValueMapperExpression();
		
		@SuppressWarnings("unchecked")
		Function<String, Object> valueMapper = fixedLengthTokenizers.expressions
				.parse(valueMapperExpression)
				.getValue(Function.class, GeneralBuilders
						.of(Suppliers.newHashMap(String.class, Object.class))
						.with(GeneralBuildingWriters.set(
								BiConsumers.forMapOf("options", Object.class), options))
						.build(), fixedLengthTokenizers.functors);
		
		return valueMapper;
	}

	@Override
	public TokenizationResult<T> split(String msgText) {
		T instance = constructors.apply(constructionArgs);
		Map<String, Object> mappingItems = new HashMap<>();
				
		int pos = 0;
		int msgTextEnd = msgText.length();
		
		for (TokenizationProperty tknztnProperty : tokenizationProperties) {
			
			String propertyPath = tknztnProperty.getPath();
			Function<String, Object> valueMapper = tknztnProperty.getValueMapper();
						
			int length = tknztnProperty.getLength();
									
			class Context {
				int pos;
				int length;
				
				Object value;
				FixedLengthTokenizationActions<?> tknztnActions;
				String tknztnKey;
				
				public Context(int pos, int length) {
					this.pos = pos < msgTextEnd ? pos : msgTextEnd;
					this.length = (pos + length) <= msgTextEnd ? length : Optional.ofNullable(msgTextEnd - pos)
							.filter(l -> l >= 0)
							.orElse(0);
				}

			}
			
			Context ctx = new Context(pos, length);

			Optional.ofNullable(valueMapper).ifPresent(vlMpr -> {
				ctx.value = Optional.ofNullable(msgText)
						.map(mt -> ctx.pos < msgTextEnd ? mt : StringUtils.EMPTY)
						.map(mt -> mt.substring(ctx.pos, ctx.pos + ctx.length))
						.map(vlMpr)
						.orElse(null);
				
				if (null == ctx.value) {
					return;
				}
				
				Optional.ofNullable(propertyPath)
				.ifPresent(pp -> {
					fixedLengthTokenizers.expressions.parse(pp)
					.setValue(instance, ctx.value);
					
					Optional.ofNullable(tknztnProperty.getTokenizationMapperName())
					.ifPresent(tknztnMprNm -> {
						ctx.tknztnActions = fixedLengthTokenizers.tokenizationActionsMappers
								.get(tknztnMprNm)
								.get(ctx.value);
						
						ctx.tknztnKey = tknztnMprNm
								.concat(":")
								.concat(ctx.value.toString());
						
					});
				});

			});
			
			pos += length;
			
			if (null == ctx.tknztnKey) {
				continue;
			}
			
			TokenizationResult<?> itemResult = ctx.tknztnActions
					.tokenize(fixedLengthTokenizers, msgText.substring(pos));
			mappingItems.put(ctx.tknztnKey, itemResult.getRoot());
			itemResult.getMappingItemEntries().forEach(e -> {
				mappingItems.put(e.getKey(), e.getValue());
			});
			
			pos += itemResult.getTokenizedTextSize();

		}
		
		TokenizationResult<T> result = GeneralBuilders.of(TokenizationResult<T>::new)
				.with(GeneralBuildingWriters.set(TokenizationResult<T>::setRoot, instance))
				.with(GeneralBuildingWriters.set(TokenizationResult<T>::setTokenizedTextSize, 
						(long) pos))
				.with(GeneralBuildingWriters.setForEach((x, e) -> x.put(e.getKey(), e.getValue()),  
						mappingItems.entrySet()))
				.build();
		
		return result;
	}

	@Override
	public Long getTotalSize() {
		return tokenizationProperties.stream()
				.map(TokenizationProperty::getLength)
				.collect(Collectors.summingLong(len -> len.longValue()));
	}

}
