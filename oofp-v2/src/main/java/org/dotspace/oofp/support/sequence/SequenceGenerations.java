package org.dotspace.oofp.support.sequence;

import lombok.RequiredArgsConstructor;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.msg.MessageSupport;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class SequenceGenerations {

	private final ExpressionEvaluations expressionEvaluations;

	private final MessageSupport messageSupport;

	private final String folderPathRoot;

	@Value("${sequence.sequence-path:sequence}")
	private String sequencePath;

	public SequenceGeneration of(SequenceGenerationOptions options) {
		return new SequenceGeneration(
				expressionEvaluations, messageSupport, folderPathRoot, sequencePath, options
		);
	}

}
