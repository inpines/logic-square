package org.dotspace.oofp.util.functional.monad;

import java.util.Map;

public class MonadBindingOperators {

	public static MonadBindingOperator getPredicate(
			String name, Object options) {
		return new MonadBindingOperator(
				MonadBindingType.PREDICATE, name, options);
	}

	public static MonadBindingOperator getMapFunction(
			String name, Object options) {
		return new MonadBindingOperator(
				MonadBindingType.MAP, name, options);
	}

	public static MonadBindingOperator getFlatMapFunction(
			String name, Object options) {
		return new MonadBindingOperator(
				MonadBindingType.FLAT_MAP, name, options);
	}

	public static MonadBindingOperator get(Map<String, Object> options) {
		return new MonadBindingOperator(
				MonadBindingType.valueOf((String) options.get("type")), 
				(String) options.get("name"), options.get("options"));
	}

}
