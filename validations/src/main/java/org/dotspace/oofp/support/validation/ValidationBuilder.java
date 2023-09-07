package org.dotspace.oofp.support.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationBuilder<T> {

    // private Class<T> clazz;
    private List<ValidationPolicy<T>> policies = new ArrayList<>();
    
	public <U> ValidationBuilder() {
	}

	public ValidationBuilder<T> adopt(ValidationPolicy<T> policy) {
        policies.add(policy);
		return this;
	}

	public List<ModelViolation> validate(T model) {
        ValidationsContext<T> ctx = new ValidationsContext<T>(model);
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
