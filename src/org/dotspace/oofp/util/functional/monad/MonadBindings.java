package org.dotspace.oofp.util.functional.monad;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;

import org.apache.commons.lang3.tuple.Pair;
import org.dotspace.oofp.support.conditional.FunctionalSupport;
import org.dotspace.oofp.util.functional.Casters;

public class MonadBindings {

	private FunctionalSupport functionalSupport;

	public <T> MonadBindingContext<T> of() {
		return new MonadBindingContext<>(functionalSupport);
	}
	
	public <T, A, R> Collector<T, A, R> getCollectors(
			Map<String, Object> options) {
		return Optional.ofNullable(options.get("collector"))
				.map(Casters.forMap(String.class, Object.class))
				.map(m -> Pair.of((String) m.get("name"), m.get("options")))
				.map(e -> functionalSupport.<T, A, R>getCollector(
						e.getLeft(), e.getRight()))
				.orElse(null);
	}
	
	public FunctionalSupport getFunctionalSupport() {
		return functionalSupport;
	}

	public void setFunctionalSupport(FunctionalSupport functionalSupport) {
		this.functionalSupport = functionalSupport;
	}

}
