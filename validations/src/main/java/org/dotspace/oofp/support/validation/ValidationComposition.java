package org.dotspace.oofp.support.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationComposition<T> {

    // private Class<T> clazz;
    private List<ValidationPolicy<T>> policies = new ArrayList<>();
    
	public <U> ValidationComposition() {
	}

	public ValidationComposition<T> with(ValidationPolicy<T> policy) {
        policies.add(policy);
		return this;
	}

	public List<GeneralViolation> validate(T model) {
        ValidationContext<T> ctx = new ValidationContext<T>(model);
        for (ValidationPolicy<T> policy : policies) {
			boolean validated = policy.validate(model, ctx);
			
			if (!validated && policy.isBrokenOnFail()) {
				ctx.interrupt();
			}
			
			if (ctx.isBroken()) {
				break;
			}

        }
		return ctx.getViolations();
	}

}
