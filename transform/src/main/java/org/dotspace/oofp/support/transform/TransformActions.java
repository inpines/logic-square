package org.dotspace.oofp.support.transform;

public class TransformActions {

	public static TransformActionContext read(String expression) {
		return new TransformActionContext(expression);
	}
	
}
