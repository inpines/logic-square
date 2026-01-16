package org.dotspace.oofp.model.dto.correlation;

import org.dotspace.oofp.support.validator.constraint.MandatoryField;
import org.dotspace.oofp.support.validator.constraint.MandatoryFieldCase;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CorrelationTestGroup {

	@NotNull
	private Integer age; 
	
	@MandatoryField(cases = { @MandatoryFieldCase(when = "age < 20"),
			@MandatoryFieldCase(when = "age >= 20", present = false, empty = true)})
	private String school;
	
	@MandatoryField(cases = { @MandatoryFieldCase(when = "age >= 20", 
			valueTest = "'teacher'.equals($$) or 'worker'.equals($$)"),
			@MandatoryFieldCase(when = "age < 20", present = false, empty = true)})
	private String job;

}
