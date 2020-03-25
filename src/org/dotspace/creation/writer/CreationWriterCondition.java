package org.dotspace.creation.writer;

import java.util.function.Predicate;

public class CreationWriterCondition<C> {

	private Predicate<C> predicate;
	
	private C cond;
	
	public CreationWriterCondition(Predicate<C> predicate, C cond) {
		this.predicate = predicate;
		this.cond = cond;
	}

	public boolean isPresent() {
		if (null == predicate) {
			return true;
		}
		
		return predicate.test(cond);
	}
}
