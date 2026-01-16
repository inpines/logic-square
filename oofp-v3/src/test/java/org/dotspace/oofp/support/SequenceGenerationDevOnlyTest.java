package org.dotspace.oofp.support;

import org.dotspace.oofp.TestConfig;

import org.dotspace.oofp.support.sequence.SequenceGeneration;
import org.dotspace.oofp.support.sequence.SequenceGenerationOptions;
import org.dotspace.oofp.support.sequence.SequenceGenerations;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.function.Function;

@SpringBootTest(classes = TestConfig.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class SequenceGenerationDevOnlyTest {

	@Autowired
	private SequenceGenerations sequenceGenerations;

	Function<Object[], RuntimeException> exceptionGenerator =
			args -> new IllegalStateException((String) args[0], (Throwable) args[1]);

	@Test
	void testSimple() {
		SequenceGeneration sg = sequenceGenerations.of(SequenceGenerationOptions.builder()
				.withBasePath("testSeq")
				.withSenderId("aSender")
				.withTypeId("aType")
				.withPatternOfFormat("${options.senderId}${options.typeId}${seqDate}#{value}")
				.build());
		try {
			String seq = sg.next(exceptionGenerator);
			
			Assertions.assertNotNull(seq);
			
			String seq1 = sg.next(exceptionGenerator);
			
			Assertions.assertNotNull(seq1);
            Assertions.assertNotEquals(seq1, seq);
			
		} catch (Throwable e) {
			Assertions.fail("fail:" + e);
		}
	}

	@Test
	void testExpression() {
		SequenceGeneration sg = sequenceGenerations.of(SequenceGenerationOptions.builder()
				.withBasePath("testSeq")
				.withSenderId("aSender")
				.withExpression("(nextSeq % 2) eq 0 ? " +
						"'E'.concat(T(org.apache.commons.lang3.StringUtils).leftPad('' + nextSeq, 5, '0')) " +
						": 'O'.concat(T(org.apache.commons.lang3.StringUtils).leftPad('' + nextSeq, 5, '0'))")
				.withPatternOfFormat("myKeyGen${options.senderId}#{value}")
				.build());

		try {

			String seq = sg.next(exceptionGenerator);

			String expect = Integer.parseInt(StringUtils.right(seq, 5)) % 2 == 0 ? "E" : "O";
			Assertions.assertNotNull(seq);
			Assertions.assertEquals(expect, seq.substring(15, 16));
			
			String seq1 = sg.next(exceptionGenerator);
			
			Assertions.assertNotNull(seq1);
            Assertions.assertNotEquals(seq1, seq);
			Assertions.assertEquals(expect.equals("E") ? "O" : "E", seq1.substring(15, 16));
			
		} catch (Throwable e) {
			Assertions.fail("fail:" + e);
		}

	}
}
