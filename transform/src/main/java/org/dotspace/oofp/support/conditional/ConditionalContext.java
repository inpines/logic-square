package org.dotspace.oofp.support.conditional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.dotspace.oofp.util.functional.FunctionalSupport;

public class ConditionalContext<R> {

	private Supplier<R> supplier;
	
	private FunctionalSupport functionalSupport;
	
	private List<String> predicates = new ArrayList<>();
	
	private List<ConditionalItem> conditionalItems = new ArrayList<>();
	
	protected ConditionalContext(Supplier<R> supplier, String predicate,
			FunctionalSupport functionalSupport) {
		this.supplier = supplier;
		this.predicates.add(predicate);
		this.functionalSupport = functionalSupport;
	}

	protected ConditionalContext(Supplier<R> supplier, 
			FunctionalSupport functionalSupport) {
		this.supplier = supplier;
		this.functionalSupport = functionalSupport;
	}

	protected ConditionalContext(Supplier<R> supplier, 
			List<String> predicates, FunctionalSupport functionalSupport) {
		this.supplier = supplier;
		this.predicates.addAll(predicates);
		this.functionalSupport = functionalSupport;
	}

	public ConditionalContext<R> withCondition(
			ConditionalItem conditionalItem) {
		conditionalItems.add(conditionalItem);
		return this;
	}
	
	public <T> Stream<R> apply(Stream<T> instances) {
		return instances.map(this::apply)
				.filter(Optional::isPresent)
				.map(Optional::get);
	}
	
	public <T> Optional<R> apply(T instance) {
		Predicate<T> predicate = predicates.stream()
				.map(prdct -> Optional.ofNullable(prdct)
						.map(functionalSupport::<T>evaluatePredicate)
						.orElse(x -> true))
				.reduce(x -> true, (p1, p2) -> p1.and(p2));
	
		Optional<T> filtering = Optional.ofNullable(instance)
				.filter(predicate);
		
		return filtering.map(x -> {
			R result = supplier.get();
			
			conditionalItems.forEach(item -> {
				
				class Context {
					Optional<?> monad = functionalSupport.evaluate(
									item.getReader(), x);
				}
				
				Context ctx = new Context();
				
				item.getTransitions().forEach(transition -> {
					ctx.monad = transit(ctx.monad, transition);
				});
				
				ctx.monad.ifPresent(v -> functionalSupport.write(result, 
						item.getWriter(), v));
			});

			return result;
		});		
	
	}
	
	private <X, Y> Optional<Y> transit(Optional<X> monad, 
			ConditionalTransition transition) {		
		Predicate<X> predicate = Optional
				.ofNullable(transition.getPredicate())
				.flatMap(expr -> monad.map(x -> functionalSupport
						.<X>evaluatePredicate(expr)))
				.orElse(x -> true);
		
		Function<X, Y> mapper = Optional
				.ofNullable(transition.getMapper())
				.flatMap(expr -> monad.map(x -> functionalSupport
						.<X, Y>evaluateFunction(expr)))
				.orElse(x -> {
					@SuppressWarnings("unchecked")
					Y yAsX = (Y) x;
					
					return yAsX;
				});
		
		return monad.filter(predicate).map(mapper);
	}
}
