package org.dotspace.oofp.support.sequence;

import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Builder(setterPrefix = "with")
class SequenceGenerationContext {

	private SequenceGenerationOptions options;

	@Builder.Default
	private String seqDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

	@Builder.Default
	private AtomicInteger nextSeq = new AtomicInteger(0);

}
