package org.dotspace.oofp.util.functional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.commons.lang3.tuple.Pair;
import org.dotspace.oofp.util.functional.stream.StreamMappers;

public class StreamContext<T, R> {

	private Stream<T> stream;
	
	private List<Pair<StreamMapper<T, R>, Predicate<T>>> mappers = new ArrayList<>();
	
	protected StreamContext(Stream<T> stream) {
		this.stream = stream;
	}

	public StreamContext<T, R> map(Function<T, R> f, Predicate<T> predicate) {
		mappers.add(Pair.of(StreamMappers.mapSingular(f), predicate));
		return this;
	}
	
	public StreamContext<T, R> flatMap(Function<T, Stream<R>> f, Predicate<T> predicate) {
		mappers.add(Pair.of(StreamMappers.mapPlural(f), predicate));
		return this;
	}
	
	public <A> Collection<R> collect(Collector<R, A, Collection<R>> collector) {
		Builder<R> builder = Stream.builder();
		
		mappers.forEach(mpr -> {
			Predicate<T> right = mpr.getRight();
			
			Predicate<T> predicate = (null == right) ? x -> true : right;
			
			StreamMapper<T, R> strmMapper = mpr.getLeft();
			
			if (null == strmMapper) {
				return;
			}
			
			Stream<T> strm = stream.filter(predicate);
			
			strmMapper.apply(strm).forEach(r -> builder.accept(r));
		});
		
		return builder.build().collect(collector);
	}
}
