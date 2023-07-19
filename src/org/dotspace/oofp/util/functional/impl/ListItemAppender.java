package org.dotspace.oofp.util.functional.impl;

import java.util.List;
import java.util.function.BiConsumer;

public class ListItemAppender<T> implements BiConsumer<List<T>, T> {

	@Override
	public void accept(List<T> l, T item) {
		l.add(item);
	}

}
