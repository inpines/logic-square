package org.dotspace.oofp.support.orm.specification;

public interface SpecificationBuilders {

	<T> SpecificationBuilder<T> forRootOf(Class<T> clazz);
		
}
