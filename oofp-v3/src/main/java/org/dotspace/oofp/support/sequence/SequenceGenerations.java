package org.dotspace.oofp.support.sequence;

import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.msg.MessageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SequenceGenerations {

	private final ExpressionEvaluations expressionEvaluations;

	private final MessageSupport messageSupport;

	private final String apiDataPathRoot;

	@Value("${sequence.sequence-path:sequence}")
	private String sequencePath;

	public SequenceGeneration of(SequenceGenerationOptions options) {
		return new SequenceGeneration(
				expressionEvaluations, messageSupport, apiDataPathRoot, sequencePath, options
		);
	}

}
