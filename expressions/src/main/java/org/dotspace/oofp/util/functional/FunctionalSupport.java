package org.dotspace.oofp.util.functional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.ExpressionEvaluations;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.util.Associable;

public class FunctionalSupport {

	private static final String VAR_OPTS = "options";

	private ExpressionEvaluations expressionEvaluations;
	
	private Associable<FunctionalSupport> functors;
	
	private Map<String, String> suppliers;
	
	private Map<String, String> predicates;
	
	private Map<String, String> functions;
	
	private Map<String, String> collectors;
		
	public <T> Supplier<T> getSupplier(String name, Object options) {
		String supplierExpression = Optional.ofNullable(name)
				.map(suppliers::get)
				.orElse(null);
		
		if (null == supplierExpression) {
			throw new IllegalArgumentException(String.format(
					"illegal argument to get supplier name of %s", name));
		}
		
		Map<String, Object> vars = GeneralBuilders
				.of(Suppliers.newHashMap(String.class, Object.class))
				.with(GeneralBuildingWriters.set(BiConsumers
						.forMapOf(VAR_OPTS, Object.class), options))
				.build();
		
		@SuppressWarnings("unchecked")
		Supplier<T> supplier = (Supplier<T>) expressionEvaluations.parse(supplierExpression)
				.getValue(Suppliers.class, vars, functors);
		
		return supplier;		
	}
	
	public <T> Predicate<T> getPredicate(String name, Object options) {
		String predicateExpression = Optional.ofNullable(name)
				.map(predicates::get)
				.orElse(null);
		
		if (null == predicateExpression) {
			throw new IllegalArgumentException(String.format(
					"illegal argument to get predicate name of %s", name));
		}
		
		@SuppressWarnings("unchecked")
		Predicate<T> predicate = expressionEvaluations.parse(predicateExpression)
				.getValue(Predicate.class, GeneralBuilders
						.of(Suppliers.newHashMap(String.class, Object.class))
						.with(GeneralBuildingWriters.set(BiConsumers
								.forMapOf(VAR_OPTS, Object.class), options))
						.build(), functors);
		return predicate;
	}

	public <T> Predicate<T> getPredicate(String name) {
		return getPredicate(name, null);
	}
	
	public <T, R> Function<T, Collection<R>> getFunctionReturnCollection(
			String name, Object options) {
		String functionExpression = Optional.ofNullable(name)
				.map(functions::get)
				.orElse(null);
		
		if (null == functionExpression) {
			throw new IllegalArgumentException(String.format(
					"illegal argument to get function name of %s", name));
		}
		
		@SuppressWarnings("unchecked")
		Function<T, Collection<R>> function = expressionEvaluations.parse(functionExpression)
				.getValue(Function.class, GeneralBuilders
						.of(Suppliers.newHashMap(String.class, Object.class))
						.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
								VAR_OPTS, Object.class), options))
						.build(), functors);
		
		return function;
	}
	
	public <T, R> Function<T, R> getFunction(String name, Object options) {
		String functionExpression = Optional.ofNullable(name)
				.map(functions::get)
				.orElse(null);
		
		if (null == functionExpression) {
			throw new IllegalArgumentException(String.format(
					"illegal argument to get function name of %s", name));
		}
		
		@SuppressWarnings("unchecked")
		Function<T, R> function = expressionEvaluations.parse(functionExpression)
				.getValue(Function.class, GeneralBuilders
						.of(Suppliers.newHashMap(String.class, Object.class))
						.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
								VAR_OPTS, Object.class), options))
						.build(), functors);
		return function;
	}

	public <T, R> Function<T, Stream<R>> getFunctionReturnStream(String name, Object options) {
		String functionExpression = Optional.ofNullable(name)
				.map(functions::get)
				.orElse(null);
		
		if (null == functionExpression) {
			throw new IllegalArgumentException(String.format(
					"illegal argument to get function name of %s", name));
		}
		
		@SuppressWarnings("unchecked")
		Function<T, Stream<R>> function = expressionEvaluations.parse(functionExpression)
				.getValue(Function.class, GeneralBuilders
						.of(Suppliers.newHashMap(String.class, Object.class))
						.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
								VAR_OPTS, Object.class), options))
						.build(), functors);
		
		return function;		
	}
	
	public <T, R> Function<T, R> getFunction(String name) {
		return getFunction(name, null);
	}

	public <R, A> Collector<R, A, Collection<R>> getCollectorOfCollection(
			String name, Object options) {
		String collectorExpression = Optional.ofNullable(name)
				.map(collectors::get)
				.orElse(null);
		
		if (null == collectorExpression) {
			throw new IllegalArgumentException(String.format(
					"illegal argument to get function name of %s", name));
		}
		
		Map<String, Object> vars = GeneralBuilders
				.of(Suppliers.newHashMap(String.class, Object.class))
				.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
						VAR_OPTS, Object.class), options))
				.build();
		
		@SuppressWarnings("unchecked")
		Collector<R, A, Collection<R>> collector = expressionEvaluations.parse(collectorExpression)
				.getValue(Collector.class, vars, functors);
		
		return collector;
	}
	
	public <T, A, R> Collector<T, A, R> getCollector(
			String name, Object options) {
		String collectionExpression = Optional.ofNullable(name)
				.map(collectors::get)
				.orElse(null);
		
		if (null == collectionExpression) {
			throw new IllegalArgumentException(String.format(
					"illegal argument to get collector name of %s", name));
		}
		
		Collector<T, A, R> collector = expressionEvaluations
				.parse(collectionExpression)
				.getValueWithVariables(GeneralBuilders
						.of(Suppliers.newHashMap(
								String.class, Object.class))
						.with(GeneralBuildingWriters.set(BiConsumers.forMapOf(
								VAR_OPTS, Object.class), options))
						.build(), functors);
		
		return collector;

	}

	public Collector<Object, Object, Object> getCollector(String name) {
		return getCollector(name, null);
	}
	
	public Map<String, String> getPredicates() {
		return predicates;
	}

	public void setPredicates(Map<String, String> predicates) {
		this.predicates = predicates;
	}

	public Map<String, String> getFunctions() {
		return functions;
	}

	public void setFunctions(Map<String, String> functions) {
		this.functions = functions;
	}

	public Map<String, String> getCollectors() {
		return collectors;
	}

	public void setCollectors(Map<String, String> collectors) {
		this.collectors = collectors;
	}

	public Object getOptions(String expression) {
		return expressionEvaluations.parse(expression).getValue(functors);
	}

	public Object getFunctors() {
		return functors;
	}

	public void setFunctors(Associable<FunctionalSupport> functors) {
		functors.associate(this);
		this.functors = functors;
	}

	public Map<String, String> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(Map<String, String> suppliers) {
		this.suppliers = suppliers;
	}

	public ExpressionEvaluations getExpressionEvaluations() {
		return expressionEvaluations;
	}

	public void setExpressionEvaluations(ExpressionEvaluations expressionEvaluations) {
		this.expressionEvaluations = expressionEvaluations;
	}

	public <X, T> Optional<T> evaluate(String expression, X x) {
		return Optional.ofNullable(expressionEvaluations.parse(expression)
				.getValue(x));
	}
	
	public <T> Predicate<T> evaluatePredicate(String expression) {
		return expressionEvaluations.parse(expression).getValue(functors);
	}

	public <T, R> Function<T, R> evaluateFunction(String expression) {
		return expressionEvaluations.parse(expression).getValue(functors);
	}

	public <T, R> void write(R result, String writer, T content) {
		if (StringUtils.isBlank(writer)) {
			return;
		}
		
		if (writer.indexOf("#value") >= 0) {
			expressionEvaluations.parse(writer).getValueWithVariables(GeneralBuilders
					.of(Suppliers.newHashMap(
							String.class, Object.class))
					.with(GeneralBuildingWriters.set(BiConsumers
							.forMapOf("value", Object.class), content))
					.build(), result);
			return;
		}
		
		expressionEvaluations.parse(writer).setValue(result, content);
	}
}