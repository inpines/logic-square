package org.dotspace.oofp.support.conditional;

import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;

public class ConditionalTransitions {

	public static ConditionalTransition map(String mapper, String predicate) {
		return GeneralBuilders.of(ConditionalTransition::new)
				.with(GeneralBuildingWriters.set(
						ConditionalTransition::setPredicate, predicate))
				.with(GeneralBuildingWriters.set(
						ConditionalTransition::setMapper, mapper))
				.build();
	}

	public static ConditionalTransition filter(String predicate) {
		return GeneralBuilders.of(ConditionalTransition::new)
				.with(GeneralBuildingWriters.set(
						ConditionalTransition::setPredicate, predicate))
				.build();
	}

	public static ConditionalTransition map(String mapper) {
		return GeneralBuilders.of(ConditionalTransition::new)
				.with(GeneralBuildingWriters.set(
						ConditionalTransition::setMapper, mapper))
				.build();
	}
}
