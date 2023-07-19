package org.dotspace.oofp.support.builder.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.dotspace.oofp.support.builder.GeneralBuildingWriter;
import org.dotspace.oofp.util.functional.Predication;

public abstract class GeneralBuildingWriterBase<T, V> implements GeneralBuildingWriter<T, V> {

	protected Predicate<T> predicate = x -> true;
	
	protected Optional<Predicate<V>> valuePredicate = Optional.empty();
		
	protected List<Predication<?>> conditionPredications = 
			new ArrayList<>();
	
	@Override
	public GeneralBuildingWriter<T, V> filterByValue(
			Predicate<V> predicate) {
		this.valuePredicate = valuePredicate.map(p -> p.and(predicate));
		return this;
	}

	@Override
	public GeneralBuildingWriter<T, V> filter(Predicate<T> predicate) {
		this.predicate = this.predicate.and(predicate);
		return this;
	}

	@Override
	public <C> GeneralBuildingWriter<T, V> filterWithCondition(
			Predicate<C> predicate, C condition) {
		if (null == predicate) {
			return this;
		}
		
		this.conditionPredications.add(Predication.of(
				predicate, condition));

		return this;
	}

	protected boolean isConditionPresent(T instance) {
		long violationCount = getViolationCount();
		
		if (violationCount > 0) {
			return false;
		}
		
		return Optional.ofNullable(instance)
				.filter(predicate)
				.isPresent();
	}

	private long getViolationCount() {
		return conditionPredications.stream()
				.filter(x -> !x.test())
				.count();
	}
	
	protected boolean isConditionPresentByValue(V value) {
		long violationCount = getViolationCount();
		
		if (violationCount > 0) {
			return false;
		}
		
		return valuePredicate.map(p -> p.test(value))
				.orElse(true);
	}
}
