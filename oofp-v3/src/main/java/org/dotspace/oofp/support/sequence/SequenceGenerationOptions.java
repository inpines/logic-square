package org.dotspace.oofp.support.sequence;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(setterPrefix = "with")
public class SequenceGenerationOptions {

	private String basePath;
	
	private String senderId;

	private String typeId;
	
	private String expression;
	
	private String patternOfFormat;

}
