package org.dotspace.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationsContext<T> {

    private T model;
    private boolean broken = false;

    private List<ModelViolation> violations = new ArrayList<>();
    private Map<String, Object> assocations = new HashMap<>();
    
    public ValidationsContext(T model) {
        this.model = model;
    }

	public boolean isBroken() {
		return broken;
	}

    public void interrupt() {
        broken = true;
    }

	public List<ModelViolation> getViolations() {
		return violations;
    }
    
    public T getModel() {
        return model;
    }

    public void add(ModelViolation violation) {
        violations.add(violation);
    }

    public Map<String, Object> getAssocations() {
        return assocations;
    }

}
