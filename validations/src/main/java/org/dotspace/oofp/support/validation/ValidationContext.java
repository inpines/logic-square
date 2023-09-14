package org.dotspace.oofp.support.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationContext<T> {

    private T model;
    private boolean broken = false;

    private List<GeneralViolation> violations = new ArrayList<>();
    private Map<String, Object> assocations = new HashMap<>();
    
    public ValidationContext(T model) {
        this.model = model;
    }

	public boolean isBroken() {
		return broken;
	}

    public void interrupt() {
        broken = true;
    }

	public List<GeneralViolation> getViolations() {
		return violations;
    }
    
    public T getModel() {
        return model;
    }

    public void add(GeneralViolation violation) {
        violations.add(violation);
    }

    public Map<String, Object> getAssocations() {
        return assocations;
    }

    public long getViolationsCount(String nameRegex) {
    	return violations.stream()
    			.filter(vltn -> {
    				Pattern p = Pattern.compile(nameRegex);
    				Matcher m = p.matcher(vltn.getValidationName());
    				if (m.matches()) {
    					return true;
    				}
    				return false;
    			})
    			.count();
    }
    
}
