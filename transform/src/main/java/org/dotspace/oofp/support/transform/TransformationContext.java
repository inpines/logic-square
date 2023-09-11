package org.dotspace.oofp.support.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.dotspace.oofp.support.ExpressionEvaluation;
import org.dotspace.oofp.support.ExpressionEvaluations;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.util.functional.BiConsumers;
import org.dotspace.oofp.util.functional.FunctionalSupport;
import org.dotspace.oofp.util.functional.Suppliers;

public class TransformationContext<T, A> {

	private ExpressionEvaluations expressionEvaluations;
	private FunctionalSupport functionalSupport; 
	
	private Function<A, T> constructor;
	private A args;
	
	private List<TransformAction> actions = new ArrayList<>();
	
	protected TransformationContext(Function<A, T> constructor, A args, 
			ExpressionEvaluations expressionEvaluations, FunctionalSupport functionalSupport) {
		this.expressionEvaluations = expressionEvaluations;
		this.functionalSupport = functionalSupport;
		this.constructor = constructor;
		this.args = args;
	}

	protected TransformationContext(Function<A, T> constructor, A args, 
			ExpressionEvaluations expressionEvaluations, FunctionalSupport functionalSupport, 
			List<TransformAction> actions) {
		this.expressionEvaluations = expressionEvaluations;
		this.functionalSupport = functionalSupport;
		this.constructor = constructor;
		this.args = args;
		this.actions = actions;
	}

	protected TransformationContext(Supplier<T> supplier, 
			ExpressionEvaluations expressionEvaluations, 
			FunctionalSupport functionalSupport) {
		this.expressionEvaluations = expressionEvaluations;
		this.functionalSupport = functionalSupport;
		this.constructor = a -> supplier.get();
		this.args = null;
	}
	
	protected TransformationContext(Supplier<T> supplier, 
			ExpressionEvaluations expressionEvaluations, 
			FunctionalSupport functionalSupport, List<TransformAction> actions) {
		this.expressionEvaluations = expressionEvaluations;
		this.functionalSupport = functionalSupport;
		this.constructor = a -> supplier.get();
		this.args = null;
		this.actions = actions;
	}
	
	public TransformationContext<T, A> with(TransformAction action) {
		actions.add(action);
		return this;
	}
	
	public <S> T transform(S source) {
		if (null == constructor) {
			return null;
		}
		
		T destination = constructor.apply(args);
		
 		actions.forEach(transform(source, destination));
		
		return destination;
	}
	
	private <S> Consumer<TransformAction> transform(S source, T destination) {
		return mbrTrnsfrmtn -> {
			Object value = expressionEvaluations.parse(mbrTrnsfrmtn.getReader())
					.getValue(source);
			
			List<MappingFunctorConfiguration> mappingFunctors = 
					mbrTrnsfrmtn.getMappers();
			
			class Ctx {
				private Optional<Object> valueOpt;
				
				public Ctx(Object value) {
					this.valueOpt = Optional.ofNullable(value);
				}
				
				public void filter(Predicate<Object> predicate) {
					valueOpt = valueOpt.filter(predicate);
				}
				
				public void map(Function<Object, Object> mapper) {
					if ("ConditionalSourceInfo".equals(source.getClass().getSimpleName())) {
						valueOpt = valueOpt.map(mapper);
					} else {
						valueOpt = "Optional.empty".equals(valueOpt.toString()) ? Optional.of("").map(mapper) : valueOpt.map(mapper);
					}

				}
				
				public Optional<Object> getValueOpt() {
					return valueOpt;
				}
			}
			
			Ctx ctx = new Ctx(value);
			
			mappingFunctors.forEach(functor -> {
				if (functor.getType() == TransformMappingType.PREDICATE) {
					Predicate<Object> predicate = Optional
							.ofNullable(functor)
							.filter(mpng -> null != mpng.getName())
							.map(mpng -> functionalSupport.getPredicate(
									mpng.getName(), functionalSupport.getOptions(
											mpng.getOptions())))
							.orElse(x -> true);
					ctx.filter(predicate);
					return;
				}
				
				Function<Object, Object> valueMapper = Optional
						.ofNullable(functor)
						.filter(mpng -> null != mpng.getName())
						.map(mpng -> functionalSupport.getFunction(mpng.getName(), 
								functionalSupport.getOptions(mpng.getOptions())))
						.orElse(x -> x);
				ctx.map(valueMapper);
			});
			
			Object mappedValue = ctx.getValueOpt().orElse(null);
			
			FunctorConfiguration collectingFunctor = mbrTrnsfrmtn
					.getCollector();
			
			Collector<Object, ?, Object> collector = Optional
					.ofNullable(collectingFunctor)
					.filter(functor -> null != functor.getName())
					.map(functor -> functionalSupport.getCollector(functor.getName(), 
							functionalSupport.getOptions(functor.getOptions())))
					.orElse(null);
			
			Object collectedValue = Optional.ofNullable(mappedValue)
					.filter(x -> null != collector)
					.map(this::toStream)
					.map(strm -> strm.collect(collector))
					.orElse(mappedValue);
			
			String writingExpr = mbrTrnsfrmtn.getWriter();
			
			if (null == writingExpr) {
				return;
			}
			
			ExpressionEvaluation exprEvaluation = expressionEvaluations.parse(writingExpr);
			if (writingExpr.contains("#value")) {
				Optional.ofNullable(collectedValue).ifPresent(cv -> {
					Map<String, Object> expressionCtx = GeneralBuilders
							.of(Suppliers.newHashMap(String.class, Object.class))
							.with(GeneralBuildingWriters.set(BiConsumers.forMapOf("value", Object.class), cv))
							.build();
					exprEvaluation.getValueWithVariables(expressionCtx, destination);
				});
				return;
			}
				
			exprEvaluation.setValue(destination, collectedValue);
		};
	}

	private <E> Stream<E> toStream(Object value) {
		Stream<E> result = null;
		
		if (null == value) {
			return null;
		}
		
		if (value instanceof Stream) {
			@SuppressWarnings("unchecked")
			Stream<E> valueAsStream = (Stream<E>) value;
			
			return valueAsStream;
		}
		
		if (value instanceof Collection) {
			@SuppressWarnings("unchecked")
			Collection<E> collection = ((Collection<E>) value);
			
			return collection.stream();
		}
		
		Class<?> clazz = value.getClass();
		
		if (clazz.isArray()) {
			@SuppressWarnings("unchecked")
			Stream<E> arrayAsStream = Arrays.stream((E[]) value);
			
			return arrayAsStream;
		}
		
		return result;
	}
	
	public FunctionalSupport getFunctionalSupport() {
		return functionalSupport;
	}

	public void setFunctionalSupport(FunctionalSupport functionalSupport) {
		this.functionalSupport = functionalSupport;
	}
	
}
