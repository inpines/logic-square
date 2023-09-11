package org.dotspace.oofp.util.functional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dotspace.oofp.support.ExpressionEvaluations;
import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.util.Associable;

public class GeneralFunctors implements Associable<FunctionalSupport> {

	private static final String SELECTOR = "selector";

	private static final String COMPARISON_RESULT = "comparisonResult";

	private static final String NAME = "name";

	private static final String PREDICATE = "predicate";

	private static final String OPTIONS = "options";

	private static final String PREDICATE_EXPRESSIONS = "predicateExpressions";

	private static final String PARAMETERS = "parameters";

	private static final String EXPRESSION = "expression";

	private static final String VALUE = "value";

	private static final String READER = "reader";

	private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

	private static final String MAPPER = "mapper";
	
	protected FunctionalSupport functionalSupport;
	protected ExpressionEvaluations expressionEvaluations;
	
	public <T> Predicate<T> byPass() {
		return x -> true;
	}
	
	public Predicate<?> isNotNull() {
		return x -> null != x;
	}
	
	public <T> Predicate<T> eq(T opts) {
		return x -> x.equals(opts);
	}
	
	public <T extends Comparable<T>> Predicate<T> gt(T opts) {
		return x -> x.compareTo(opts) > 0;
	}
	
	public <T> Predicate<T> not(Predicate<T> predicate) {
		return x -> !predicate.test(x);
	}
	
	public <T> Predicate<Collection<T>> all(Predicate<T> predicate) {
		return x -> x.stream().allMatch(predicate);
	}
	
	public <T> Predicate<Collection<T>> any(Predicate<T> predicate) {
		return x -> x.stream().anyMatch(predicate);
	}
	
	public <T> Function<T, T> identity() {
		return Function.identity();
	}
	
	public <T>Function<T, String> toTextOfDecimal() {
		return x -> x.toString();
	}
	
	public Function<Long, Date> toDateTime() {
		return x -> {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
			LocalDateTime ldt = LocalDateTime.parse(x.toString(), formatter);
			
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		};
	}
	
	public <T> Function<Collection<T>, T> getFirst() {
		return x -> x.stream().findFirst().orElse(null);
	}
	
	public <T> Function<Collection<T>, T> getLast() {
		return x -> x.stream().findFirst().orElse(null);
	}
	
	public Function<Number, BigDecimal> multiplyBy(Number multiplier) {
		return x -> Optional.ofNullable(multiplier)
				.map(this::getBigDecimal)
				.map(y -> getBigDecimal(x).multiply(y))
				.orElse(null);
	}
	
	private BigDecimal getBigDecimal(Number n) {
		if (n instanceof Integer) {
			return new BigDecimal(n.intValue());
		}
		
		if (n instanceof Long) {
			return new BigDecimal(n.longValue());
		}
		
		if (n instanceof Double || n instanceof Float) {
			return new BigDecimal(n.doubleValue());
		}
		
		return null;
	}
	
	public Function<String, String> leftPad(Map<String, Object> options) {
		Pair<Integer, String> lenPad = getPadOptions(options, "0");
		return x -> StringUtils.leftPad(x, lenPad.getLeft(), lenPad.getRight());
	}

	public <T>Function<T, String> toStrLeftPad(Map<String, Object> options) {
		Pair<Integer, String> lenPad = getPadOptions(options, "0");
		return x -> StringUtils.leftPad(!Objects.nonNull(x) ? "0" : String.valueOf(x), lenPad.getLeft(), lenPad.getRight());
	}
	
	public Pair<Integer, String> getPadOptions(Map<String, Object> options, String defaultPadding) {
		Integer len = Optional.ofNullable(options.get("length"))
				.map(Casters.cast(Integer.class))
				.orElse(null);
		String padding = Optional.ofNullable(options.get("padding"))
				.map(Casters.forText())
				.orElse(defaultPadding);
		return Pair.of(len, padding);
	}
	
	public Function<String, String> rightPad(Map<String, Object> options) {
		Pair<Integer, String> lenPad = getPadOptions(options, " ");
		return x -> StringUtils.rightPad(x, lenPad.getLeft(), lenPad.getRight());
	}

	public <T>Function<T, String> toStrRightPad(Map<String, Object> options) {
		Pair<Integer, String> lenPad = getPadOptions(options, " ");
		return x -> StringUtils.rightPad(!Objects.nonNull(x) ? "" : String.valueOf(x), lenPad.getLeft(), lenPad.getRight());
	}
	
	public Collector<Object, ?, Long> count() {
		return Collectors.counting();
	}
	
	public Collector<Object, ?, Integer> countToInteger() {
		return Collectors.mapping(x -> 1, Collectors.reducing(0, (x, i) -> x + i));
	}
	
	public <T> Collector<T, ?, T> first() {
		return Collectors.reducing(null, (l, r) -> l);
	}
	
	public <T> Collector<T, ?, T> last() {
		return Collectors.reducing(null, (l, r) -> r);
	}
	
	public Collector<Long, ?, Long> totalLong() {
		return Collectors.reducing(0L, (l, r) -> l + r);
	}
	
	public Collector<Number, ?, Double> average() {
		return Collectors.averagingDouble(x -> x.doubleValue());
	}
	
	public <T> Collector<T, ?, List<T>> toList() {
		return Collectors.toList();
	}
	
	public <T> Collector<T, ?, Set<T>> toSet() {
		return Collectors.toSet();
	}
	
	public <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(Map<String, Object> options) {
		Function<T, K> classifier = Optional
				.ofNullable(options.get("keyClassifier"))
				.map(Casters.forText())
				.map(clzfr -> functionalSupport.<T, K>getFunction(clzfr))
				.orElse(null);
		return Collectors.groupingBy(classifier);
	}
	
	public <T> Collector<CharSequence, ?, String> joining(
			Map<String, Object> opts) {
		if (null == opts) {
			return Collectors.joining();
		}
		
		String delimiter = Optional.ofNullable(opts.get("detlimiter"))
				.map(Casters.forText())
				.orElse(StringUtils.EMPTY);
		String prefix = Optional.ofNullable(opts.get("prefix"))
				.map(Casters.forText())
				.orElse(StringUtils.EMPTY);
		String suffix = Optional.ofNullable(opts.get("suffix"))
				.map(Casters.forText())
				.orElse(StringUtils.EMPTY);
		
		return Collectors.joining(delimiter, prefix, suffix);
				
	}
	
	public <T, U> Predicate<T> equalsReading(Map<String, Object> opts) {
		Function<T, Optional<U>> reader = x -> readValue(opts, x);
				
		return x -> Optional.ofNullable(x).flatMap(reader)
				.filter(v -> {
					Object vl = getValue(x, opts);

					return v.equals(vl);
				})
				.isPresent();
	}

	protected <T, U> Optional<U> readValue(Map<String, Object> opts, T x) {
		Object reader = opts.get(READER);
		
		if (reader instanceof String) {
			return Optional.ofNullable(reader)
					.map(Casters.forText())
					.map(expr -> expressionEvaluations.parse(expr).getValue(x));
		}
		
		if (reader instanceof List) {
			
			class Context {
				Optional<Object> value;
				
				Context(Object value) {
					this.value = Optional.ofNullable(value);
				}
				
				void map(Pair<String, Object> reader) {
					Map<String, Object> variables = Optional
							.ofNullable(reader.getRight())
							.map(opts -> constructVariables(reader))
							.orElse(Collections.emptyMap());
					
					this.value = this.value
							.map(v -> expressionEvaluations.parse(reader.getLeft())
									.getValueWithVariables(variables, v));
				}

				private Map<String, Object> constructVariables(
						Pair<String, Object> reader) {
					return GeneralBuilders.of(Suppliers.newHashMap(
							String.class, Object.class)).with(
									GeneralBuildingWriters.set(BiConsumers.forMapOf(
											OPTIONS, Object.class), 
											reader.getRight()))
							.build();
				}
			}

			Context ctx = new Context(x);
			
			
			Stream<?> readerStream = ((List<?>) reader).stream();
			
			readerStream
			.map(rdr -> {
				if (rdr instanceof String) {
					return Optional.ofNullable(Pair.of((String)rdr, null));
				}
				
				if (rdr instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> rdrAsMap = (Map<String, Object>) rdr;
					Pair<String, Object> result = Pair.of(
							(String) rdrAsMap.get(NAME), rdrAsMap.get(OPTIONS));
					return Optional.ofNullable(result);
				}
				
				return Optional.<Pair<String, Object>>empty();
			})
			.filter(Optional::isPresent)
			.map(Optional::get)
			.forEach(rdr -> ctx.map(rdr));
				
			@SuppressWarnings("unchecked")
			Optional<U> result = (Optional<U>) ctx.value;
			
			return result;
		}
		
		return Optional.empty();
	}
	
	private <T, V> V getValue(T x, Map<String, Object> opts) {
		@SuppressWarnings("unchecked")
		V value = (V) opts.get("constant");
		
		return Optional.ofNullable(opts.get(VALUE))
				.map(Casters.forText())
				.map(expr -> expressionEvaluations.parse(expr).<V>getValue(x))
				.orElse(Optional.ofNullable(value).orElse(null));
	}
	
	public <T> Predicate<T> notEqualsReading(Map<String, Object> opts) {
		Predicate<T> predicate = equalsReading(opts);
		return predicate.negate();
	}
	
	public <T> Predicate<T> validate(Map<String, Object> options) {
		String expression = (String) options.get(EXPRESSION);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> parameters = (Map<String, Object>) options.get(PARAMETERS);
		
		return x -> expressionEvaluations.parse(expression).getValue(Boolean.class, parameters , x);
	}
	
	public <T> Predicate<T> allMatch(Map<String, Object> options) {
		@SuppressWarnings("unchecked")
		List<String> predicateExpressions = (List<String>) options.get(PREDICATE_EXPRESSIONS);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> parameters = (Map<String, Object>) options.get(PARAMETERS);
		
		return x -> predicateExpressions.stream()
				.allMatch(expr -> expressionEvaluations.parse(expr)
						.getValue(Boolean.class, parameters, x));
	}
	
	public <T> Predicate<T> anyMatch(Map<String, Object> options) {
		@SuppressWarnings("unchecked")
		List<String> predicateExpressions = (List<String>) options.get(PREDICATE_EXPRESSIONS);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> parameters = (Map<String, Object>) options.get(PARAMETERS);
		
		return x -> predicateExpressions.stream()
				.anyMatch(expr -> expressionEvaluations.parse(expr)
						.getValue(Boolean.class, parameters, x));
	}
	
	public <T> Function<T, Object> lookup(Map<String, Object> options) {
		String expression = (String) options.get("expressions");
		
		@SuppressWarnings("unchecked")
		Map<String, Object> parameters = (Map<String, Object>) options.get(PARAMETERS);
		
		return x -> expressionEvaluations.parse(expression).getValueWithVariables(parameters, x);
	}
	
	public <T> Predicate<T> in(T[] values) {
		return x -> Arrays.asList(values).stream()
				.anyMatch(v -> v.equals(x));
	}

	public <T> Predicate<T> filterReading(Map<String, Object> opts) {
		return x -> {
			Predicate<T> predicate = Optional.ofNullable(opts.get(PREDICATE))
					.map(Casters.forMap(String.class, Object.class))
					.map(m -> functionalSupport.<T>getPredicate(
							(String) m.get(NAME), m.get(OPTIONS)))
					.orElse(null);
			
			if (null == predicate) {
				return false;
			}
			
			return Optional.ofNullable(x).filter(predicate).isPresent();
		};
	}
	
	public <T> Predicate<T> isNotNullReading(Map<String, Object> opts) {
		
		return x -> {
			Optional<T> propValue = this.<T, T>readValue(opts, x);

			return Objects.nonNull(propValue.orElse(null));
		};
		
	}
	
	public <T> Predicate<T> isNullReading(Map<String, Object> opts) {
		return this.<T>isNotNullReading(opts).negate();
	}
	
	public <T, V> Predicate<T> equalsReadingIn(Map<String, Object> opts) {
		return x -> {
			Optional<V> propValue = this.<T, V>readValue(opts, x);
			
			return Optional.ofNullable(opts.get("values"))
					.map(Casters.forList(Object.class))
					.orElse(Collections.emptyList())
					.stream()
					.anyMatch(value -> value.equals(propValue.orElse(null)));
		};
	}
	
	public <T, V extends Comparable<V>> Predicate<T> compareReadingSelection(Map<String, Object> opts) {
		return x -> {
			Optional<Collection<V>> values = this.<T, Collection<V>>readValue(
					opts, x);
			
			Function<Collection<V>, V> selector = Optional
					.ofNullable(opts.get(SELECTOR))
					.map(Casters.forMap(String.class, Object.class))
					.map(m -> functionalSupport.<Collection<V>, V>getFunction(
							(String) m.get(NAME), m.get(OPTIONS)))
					.orElse(null);
			
			if (null == selector) {
				return false;
			}
			
			V value = values.map(selector).orElse(null);
			
			V vl = getValue(x, opts);
			
			int result = value.compareTo(vl);
			
			if (result == (Integer) opts.get(COMPARISON_RESULT)) {
				return true;
			}
			
			return false;
		};
	}
	
	public <T, V extends Comparable<V>> Predicate<T> compareReading(
			Map<String, Object> opts) {
		return x -> {
			Optional<V> value = this.<T, V>readValue(opts, x);
			
			V vl = getValue(x, opts);
			
			Optional<Integer> result = value.map(v -> v.compareTo(vl));
			
			return result
					.filter(r -> r == ((Integer) opts.get(COMPARISON_RESULT)).intValue())
					.isPresent();
		};
	}
	
	public <T> Predicate<T> allMatchReading(List<Map<String, Object>> listOfOpts) {
		
		List<Predicate<Object>> predicates = getReadingPredicates(listOfOpts);
		
		return x -> predicates.stream().allMatch(p -> p.test(x));
	}

	private List<Predicate<Object>> getReadingPredicates(List<Map<String, Object>> listOfOpts) {
		return listOfOpts.stream()
				.flatMap(this::getPredicateWithOptsStream)
				.map(this::getPredicateWhereOpts)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}
	
	public <T> Predicate<T> anyMatchReading(List<Map<String, Object>> listOfOpts) {
		List<Predicate<Object>> predicates = getReadingPredicates(listOfOpts);

		return x -> predicates.stream().anyMatch(p -> p.test(x));
	}
	
	private Optional<Predicate<Object>> getPredicateWhereOpts(Pair<String, Object> prdctWithOpts) {
		Optional<Predicate<Object>> predicate = Optional.ofNullable(prdctWithOpts)
				.map(pwo -> functionalSupport.getPredicate(pwo.getLeft(), pwo.getRight()));
				
		return predicate;
	}
		
	public <T, V> Function<T, V> withReading(Map<String, Object> opts) {
		return x -> {
			V value = this.<T, V>readValue(opts, x).orElse(null);
			
			return value;
		};
	}
	
	private Stream<Pair<String, Object>> getPredicateWithOptsStream(Map<String, Object> opts) {
		
		String prdctnm = getPredicateName(opts);
		Object prdctopts = opts.get(OPTIONS);
		
		if (prdctopts instanceof Map) {
			
			return Stream.<Pair<String, Object>>builder()
					.add(Pair.of(prdctnm, prdctopts))
					.build();
		}
		
		if (opts instanceof List) {
			List<?> optsAsList = (List<?>) opts;
			
			return optsAsList.stream()
					.map(Casters.forMap(String.class, Object.class))
					.map(o -> Pair.of(prdctnm, o.get(OPTIONS)));
		}
		
		return Stream.empty();
		
	}
	
	private String getPredicateName(Map<String, Object> opts) {
		return Optional.ofNullable(opts.get(PREDICATE))
				.map(Casters.forText())
				.orElse(StringUtils.EMPTY);
	}
	
	public <T, R> Function<Collection<T>, StreamContext<T, R>> constructStream() {
		return x -> new StreamContext<>(x.stream());
	}
	
	public <T, R> Function<StreamContext<T, R>, StreamContext<T, R>> mapStream(Map<String, Object> options) {
		@SuppressWarnings("unchecked")
		Function<T, R> f = (Function<T, R>) Optional.ofNullable(options.get(MAPPER))
				.map(Casters.forMap(String.class, Object.class))
				.map(mapper -> functionalSupport.getFunction((String) mapper.get(NAME), mapper.get(OPTIONS)))
				.orElse(null);
		
		Predicate<T> predicate = findPredicate(options);
		
		return x -> x.map(f, predicate);
	}

	private <T> Predicate<T> findPredicate(Map<String, Object> options) {
		@SuppressWarnings("unchecked")
		Predicate<T> result = (Predicate<T>) Optional.ofNullable(options.get(PREDICATE))
				.map(Casters.forMap(String.class, Object.class))
				.map(prdct -> functionalSupport.getPredicate((String) prdct.get(NAME), prdct.get(OPTIONS)))
				.orElse(null);
		
		return result;
	}
	
	public <T, R> Function<StreamContext<T, R>, StreamContext<T, R>> flatMapStream(Map<String, Object> options) {
		Function<T, Stream<R>> f = Optional.ofNullable(options.get(MAPPER))
				.map(Casters.forMap(String.class, Object.class))
				.map(m -> functionalSupport.<T, R>getFunctionReturnStream((String) m.get(NAME), m.get(OPTIONS)))
				.orElse(null);
		
		Predicate<T> predicate = findPredicate(options);
		
		return x -> x.flatMap(f, predicate);
	}

	public <T, A, R> Function<StreamContext<T, R>, Collection<R>> collectStream(Map<String, Object> options) {
		Optional<Collector<R, A, Collection<R>>> collector = Optional.ofNullable(options.get("collector"))
				.map(Casters.forMap(String.class, Object.class))
				.map(m -> functionalSupport.getCollectorOfCollection((String) m.get(NAME), m.get(OPTIONS)));
		
		return x -> collector.map(clctr -> x.collect(clctr)).orElse(null);
	}
	
	public <K, T> Function<Map<K, T>, Collection<T>> getValueCollectionOfMap() {
		return x -> x.values();
	}
	
	public <K, T> Function<Map<K, T>, Set<Pair<K, T>>> getEntrySetOfMap() {
		return x -> x.entrySet().stream()
				.map(e -> Pair.of(e.getKey(), e.getValue()))
				.collect(Collectors.toSet());
	}
	
	public <T, E> Function<T, Stream<E>> streamize() {
		return x -> {
			if (x instanceof Collection) {
				@SuppressWarnings("unchecked")
				Collection<E> collection = (Collection<E>) x;
				
				return getStreamOfCollection(collection);
			}
			
			if (x instanceof Map) {
				@SuppressWarnings("unchecked")
				Stream<E> entrySet = (Stream<E>) getEntrySetOfMap((Map<?, ?>) x);
				
				return entrySet;
			}
			
			Class<?> clazz = x.getClass();
			if (clazz.isArray()) {
				@SuppressWarnings("unchecked")
				E[] array = (E[]) x;
				
				return Arrays.stream(array);
			}
			return null;
		};
	}
	
	private <K, T> Stream<Pair<K, T>> getEntrySetOfMap(Map<K, T> m) {
		return m.entrySet().stream()
				.map(e -> Pair.of(e.getKey(), e.getValue()));
	}

	private <E> Stream<E> getStreamOfCollection(Collection<E> collection) {
		return collection.stream();
	}

	public <T> Function<Stream<T>, Stream<T>> withFilterInStream(Map<String, Object> options) {
		Predicate<T> predicate = Optional.ofNullable(options.get(PREDICATE))
				.map(Casters.forMap(String.class, Object.class))
				.map(m -> functionalSupport.<T>getPredicate((String) m.get(NAME), m.get(OPTIONS)))
				.orElse(t -> true);
		
		return x -> x.filter(predicate);
	}
	
	public <T, R> Function<Stream<T>, Stream<R>> withMappingInStream(Map<String, Object> options) {
		Function<T, R> f = findMapper(options);
		
		return x -> x.map(f);
	}

	private <T, R> Function<T, R> findMapper(Map<String, Object> options) {
		Function<T, R> f = Optional.ofNullable(options.get(MAPPER))
				.map(Casters.forMap(String.class, Object.class))
				.map(m -> functionalSupport.<T, R>getFunction((String) m.get(NAME), m.get(OPTIONS)))
				.orElse(t -> null);
		return f;
	}
	
	public <T, R> Function<Stream<T>, Stream<R>> withFlatMappingInStream(Map<String, Object> options) {
		Function<T, Stream<R>> f = findMapper(options);
		
		return x -> x.flatMap(f);
	}
	
	public <T, R> Function<CompletableFuture<T>, CompletableFuture<R>> withThenApplyingInFuture(
			Map<String, Object> options) {
		Function<T, R> f = findMapper(options);		
		Executor taskExecutor = findTaskExecutor(options);
		
		return x -> taskExecutor == null ? x.thenApplyAsync(f) : x.thenApplyAsync(f, taskExecutor);
	}

	private Executor findTaskExecutor(Map<String, Object> options) {
		return Optional.ofNullable(options.get("taskExecutor"))
				.map(Casters.cast(Executor.class))
				.orElse(null);
	}
	
	public <T, R> Function<CompletableFuture<T>, CompletableFuture<R>> withThenComposingInFuture(
			Map<String, Object> options) {
		Function<T, CompletionStage<R>> f = findMapper(options);
		Executor taskExecutor = findTaskExecutor(options);
		
		return x -> taskExecutor == null ? x.thenComposeAsync(f) : x.thenComposeAsync(f, taskExecutor);
	}
	
	public <T> Function<CompletableFuture<T>, T> withJoiningInFuture() {
		return x -> x.join();
	}
	
	public <T> Function<CompletableFuture<T>, T> withGettingInFuture(Long timeout) {
		return x -> getFutureResult(x, timeout);
	}
	
	private <T> T getFutureResult(CompletableFuture<T> future, Long timeout) {
		try {
			return timeout != null ? future.get(timeout.longValue(), TimeUnit.SECONDS): future.get();
		} catch (Throwable e) {
			return null;
		}
	}
	
	public <T> Function<T, T> asValueIfNull(T value) {
		return x -> Optional.ofNullable(x).orElse(value);
	}
	
	public <T> Function<T, String> format(String expression) {
		class Context {
			int i = 0;
			StringBuilder result = new StringBuilder();
		}
		
		Context ctx = new Context();
		
		return x -> {
			Pattern p = Pattern.compile("\\$\\{([^\\{\\}]*)\\}");

			Matcher m = p.matcher(expression);
			while (m.find()) {
				String prop = m.group(1);
				Object value = expressionEvaluations.parse(prop).getValue(x);
				
				ctx.result.append(expression.subSequence(ctx.i, m.start()));
				ctx.result.append(value.toString());
				ctx.i = m.end();
			}
			ctx.result.append(expression.substring(ctx.i));
			return ctx.result.toString();
		};
	}
	
	public <T, R> Function<T, R> read(String expression) {
		return x -> expressionEvaluations.parse(expression).getValue(x);
	}
	
	@Override
	public void associate(FunctionalSupport functionalSupport) {
		this.functionalSupport = functionalSupport;
	}

	public FunctionalSupport getFunctionalSupport() {
		return functionalSupport;
	}
	
}
