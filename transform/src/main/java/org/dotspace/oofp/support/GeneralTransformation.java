package org.dotspace.oofp.support;
import org.dotspace.oofp.support.dto.GeneralTransformationRequest;

public interface GeneralTransformation {

	public <P, T> T transform(GeneralTransformationRequest<P, T> request);
	
}
