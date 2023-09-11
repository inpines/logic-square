package org.dotspace.oofp.support.transform;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.ExpressionEvaluations;
import org.dotspace.oofp.support.GeneralTransformation;
import org.dotspace.oofp.support.dto.GeneralTransformationRequest;
import org.dotspace.oofp.util.functional.Casters;
import org.dotspace.oofp.util.functional.FunctionalSupport;

public class GeneralTransformationImpl implements GeneralTransformation {

	private FunctionalSupport functionalSupport;
	private ExpressionEvaluations expressionEvaluations; 
	
	@Override
	public <P, T> T transform(GeneralTransformationRequest<P, T> request) {
		
		return Optional.ofNullable(request)
				.map(req -> {
			
			return transform(req.getDestinations(), req.getDestinationPatameters(),
					req.getTransformMappers(), req.getSourceInfo());	
		})
		.orElse(null);
	}

	private <P, T> T transform(Function<P, T> destinations, P parameters,
			List<TransformMapping> transformMappers, Object instance) {
		
		T result = Optional.ofNullable(destinations)
				.map(dstntns -> dstntns.apply(parameters))
				.orElse(null);
		
		if (null == result) {
			return result;
		}
		
		transformMappers.stream()
		.sorted((l, r) -> compare(TransformMapping::getMapperItemSequence, l, r))
		.forEach(tm -> transformItem(tm, instance, result));
		
		return result;
	}

	private <V extends Comparable<V>> int compare(Function<TransformMapping, V> getter, 
			TransformMapping left, TransformMapping right) {
		Integer leftComparsion = Optional.ofNullable(left)
				.map(getter)
				.map(v -> v.compareTo(Optional.ofNullable(right)
						.map(getter).orElse(null)))
				.orElse(null);
		
		if (null != leftComparsion) {
			return leftComparsion;
		}
		
		Integer rightComparsion = Optional.ofNullable(right)
				.map(getter)
				.map(v -> v.compareTo(Optional.ofNullable(left)
						.map(getter).orElse(null)))
				.orElse(null);
		
		if (null != rightComparsion) {
			return rightComparsion;
		}
		
		return 0;
		
	}
	
	private <T> void transformItem(TransformMapping transformMapper, 
			Object srcInfo, T result) {
		
		Object sourcePropValue = getSourceExpressionValue(transformMapper, srcInfo);
		
		Predicate<Object> predicate = createPredicate(transformMapper);
		
		if (!Optional.ofNullable(sourcePropValue).filter(predicate).isPresent()) {
			return;
		}
		
		if (!StringUtils.isBlank(transformMapper.getIndicator())) {
			
			if (sourcePropValue instanceof List) {
				int index = Integer.parseInt(transformMapper.getIndicator());
				sourcePropValue = ((List<?>) sourcePropValue).get(index);
			}
			else if (sourcePropValue instanceof Map) {
				String key = transformMapper.getIndicator();
				sourcePropValue = Optional.ofNullable(sourcePropValue)
						.map(Casters.forMap(String.class, Object.class))
						.map(m -> m.get(key))
						.orElse(null);
			}
		}
		
		Function<Object, Object> valueMapper = createValueMapper(transformMapper);
		
		Object mappedValue = Optional.ofNullable(sourcePropValue).map(valueMapper).orElse(null);
		
		if (!StringUtils.isBlank(transformMapper.getCollectorName())) {
			Collector<Object, ?, Object> collector = createValuesCollector(transformMapper);
			if (mappedValue instanceof Collection) {
				@SuppressWarnings("unchecked")
				Collection<Object> collection = (Collection<Object>) mappedValue;
				
				mappedValue = collection.stream().collect(collector);
			}
			else if (mappedValue instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) mappedValue;
				
				mappedValue = map.values().stream().collect(collector);
			}
		}		
		
		expressionEvaluations.parse(transformMapper.getDestinationExpression()).setValue(
				result, mappedValue);
	}

	private Collector<Object, Object, Object> createValuesCollector(TransformMapping transformMapper) {
		String collectorName = transformMapper.getCollectorName();
		String collectorOptions = transformMapper.getCollectorOptions();
		
		if (collectorName == null) {
			return null;
		}
		
		return Optional.ofNullable(collectorOptions)
				.map(optsExpr -> functionalSupport
						.getCollector(collectorName, getOptions(collectorOptions)))
				.orElse(functionalSupport.getCollector(collectorName));			
	}

	private Function<Object, Object> createValueMapper(TransformMapping transformMapper) {
		String valueMapperName = transformMapper.getValueMapperName();
		String valueMapperOptions = transformMapper.getValueMapperOptions();
		
		if (null == valueMapperName) {
			return x -> x;
		}
		
		return Optional.ofNullable(valueMapperOptions)
				.map(expr -> functionalSupport.getFunction(valueMapperName, getOptions(expr)))
				.orElse(functionalSupport.getFunction(valueMapperName));
	}

	private Predicate<Object> createPredicate(TransformMapping transformMapper) {
		String predicateName = transformMapper.getPredicateName();
		
		if (null == predicateName) {
			return x -> true;
		}
		
		return Optional.ofNullable(transformMapper.getPredicateOptions())
				.map(expr -> {
					return functionalSupport.getPredicate(
							predicateName, getOptions(expr));
				})
				.orElse(functionalSupport.getPredicate(predicateName));
	}

	private Object getOptions(String optionsExpression) {
		return expressionEvaluations.parse(optionsExpression)
				.getValue();
	}

	private Object getSourceExpressionValue(TransformMapping transformMapper, Object instance) {
		return expressionEvaluations.parse(transformMapper.getSourceExpression())
				.getValueWithVariables(Collections.emptyMap(), instance);
	}

	public FunctionalSupport getFunctionalSupport() {
		return functionalSupport;
	}

	public void setFunctionalSupport(FunctionalSupport functionalSupport) {
		this.functionalSupport = functionalSupport;
	}

	public ExpressionEvaluations getExpressionEvaluations() {
		return expressionEvaluations;
	}

	public void setExpressionEvaluations(ExpressionEvaluations expressionEvaluations) {
		this.expressionEvaluations = expressionEvaluations;
	}

}
