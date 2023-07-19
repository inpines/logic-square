package org.dotspace.oofp.util.functional.monad;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.dotspace.oofp.support.conditional.FunctionalSupport;

public class MonadBindingContext<T> {

	private List<MonadBindingOperator> operators = new ArrayList<>();
	private FunctionalSupport functionSupport;
	
	protected MonadBindingContext(FunctionalSupport functionalSupport) {
		this.functionSupport = functionalSupport;
	}

	public MonadBindingContext<T> withFilter(
			String name, Object options) {
		operators.add(MonadBindingOperators
				.getPredicate(name, options));
		return this;
	}
	
	public MonadBindingContext<T> withMapping(
			String name, Object options) {
		operators.add(MonadBindingOperators
				.getMapFunction(name, options));
		return this;
	}

	public MonadBindingContext<T> withFlatMapping(
			String name, Object options) {
		operators.add(MonadBindingOperators
				.getFlatMapFunction(name, options));
		return this;
	}
	
	public MonadBindingContext<T> withBinding(
			MonadBindingOperator operator) {
		if (operator.getType() == MonadBindingType.PREDICATE) {
			return withFilter(operator.getName(), operator.getOptions());
		}
		
		if (operator.getType() == MonadBindingType.MAP) {
			return withMapping(operator.getName(), operator.getOptions());
		}
		
		if (operator.getType() == MonadBindingType.FLAT_MAP) {
			return withFlatMapping(operator.getName(), operator.getOptions());
		}
		return null;
	}
	
	public MonadBindingContext<T> withBinding(
			List<MonadBindingOperator> operators) {
		operators.forEach(this::withBinding);
		
		return this;
	}
	
	public <X> Optional<X> apply(Optional<T> monad) {
		
		class Context {
			private Optional<?> binded = monad;
			
			public void filter(MonadBindingOperator oprator) {
				binded = binded.filter(functionSupport.getPredicate(
						oprator.getName(), oprator.getOptions()));
			}
			
			public void map(MonadBindingOperator oprator) {
				binded = binded.map(functionSupport.getFunction(
						oprator.getName(), oprator.getOptions()));				
			}
			
			public void flatMap(MonadBindingOperator oprator) {
				binded = binded.flatMap(functionSupport.getFunction(
						oprator.getName(), oprator.getOptions()));								
			}
			
			public Optional<X> getResult() {
				@SuppressWarnings("unchecked")
				Optional<X> result = (Optional<X>) binded;
				
				return result;
			}
		}
		
		Context ctx = new Context();
		
		operators.forEach(oprtr -> {
			if (oprtr.getType() == MonadBindingType.PREDICATE) {
				ctx.filter(oprtr);
				return;
			}
			
			if (oprtr.getType() == MonadBindingType.MAP) {
				ctx.map(oprtr);
			}
			
			if (oprtr.getType() == MonadBindingType.FLAT_MAP) {
				ctx.flatMap(oprtr);
			}

		});
		
		Optional<X> result = ctx.getResult();
		
		return result;
	}
	
	public <X> X apply(Optional<T> monad, X valueIfNull) {
	
		return this.<X>apply(monad).orElse(valueIfNull);

	}
	
	public <X> Stream<X> apply(Stream<T> monad) {
		
		class Context {
			
			private Stream<?> monad;
			
			public Context(Stream<T> monad) {
				this.monad = monad;
			}
			
			public void filter(MonadBindingOperator oprator) {
				monad = monad.filter(functionSupport.getPredicate(
						oprator.getName(), oprator.getOptions()));
			}
			
			public void map(MonadBindingOperator oprator) {
				monad = monad.map(functionSupport.getFunction(
						oprator.getName(), oprator.getOptions()));				
			}
			
			public void flatMap(MonadBindingOperator oprator) {
				monad = monad.flatMap(functionSupport.getFunction(
						oprator.getName(), oprator.getOptions()));								
			}
			
			public Stream<X> getResult() {
				@SuppressWarnings("unchecked")
				Stream<X> result = (Stream<X>) monad;
				
				return result;
			}
		}
		
		Context ctx = new Context(monad);
		
		operators.forEach(oprtr -> {
			if (oprtr.getType() == MonadBindingType.PREDICATE) {
				ctx.filter(oprtr);
				return;
			}
			
			if (oprtr.getType() == MonadBindingType.MAP) {
				ctx.map(oprtr);
			}
			
			if (oprtr.getType() == MonadBindingType.FLAT_MAP) {
				ctx.flatMap(oprtr);
			}
		});
		
		Stream<X> bindingResult = ctx.getResult();
		
		return bindingResult;
	}
	
	public <X, R> R collect(Stream<T> monad, 
			Pair<String, Object> collectorEntry) {
		
		Stream<X> bindingResult = apply(monad);
		
		Collector<X, ?, R> collector = functionSupport.getCollector(
				collectorEntry.getLeft(), collectorEntry.getRight());
		
		return bindingResult.collect(collector);
	}
	
	public <X, A, R> R collect(
			Stream<T> monad, Collector<X, A, R> collector) {
		Stream<X> bindingResult = apply(monad);
		
		return bindingResult.collect(collector);		
	}

}
