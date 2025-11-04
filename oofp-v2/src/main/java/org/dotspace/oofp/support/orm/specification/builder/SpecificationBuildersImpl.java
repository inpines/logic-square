package org.dotspace.oofp.support.orm.specification.builder;

import org.dotspace.oofp.support.orm.specification.SpecificationBuilder;
import org.dotspace.oofp.support.orm.specification.SpecificationBuilders;
import org.springframework.stereotype.Component;

@Component
public class SpecificationBuildersImpl implements SpecificationBuilders {

	@Override
	public <T> SpecificationBuilder<T> forRootOf(Class<T> clazz) {
		return new SpecificationBuilderImpl<>();
	}

}
