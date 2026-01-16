package org.dotspace.oofp.model.dto.correlation;

import java.io.Serializable;

import org.dotspace.oofp.support.validator.constraint.Correlation;

import lombok.Data;

@Data
public class CorrelationTestDto implements Serializable {

	@Correlation
	private CorrelationTestGroup correlationGroup;

}
