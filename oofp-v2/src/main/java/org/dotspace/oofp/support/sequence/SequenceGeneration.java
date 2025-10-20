package org.dotspace.oofp.support.sequence;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.msg.MessageSupport;
import org.dotspace.oofp.utils.functional.monad.Maybe;

@AllArgsConstructor
public class SequenceGeneration {

	private final ExpressionEvaluations expressionEvaluations;
	private final MessageSupport messageSupport;
	private String folderPathRoot;
	private String sequencePath;
	private SequenceGenerationOptions options;

	public String next() {
		
		SequenceGenerationContext ctx = SequenceGenerationContext.builder()
				.withOptions(options)
				.build();

		Path root = Maybe.given(folderPathRoot)
				.map(Paths::get)
				.map(path -> path.resolve(sequencePath))
				.orElseThrow(() -> SequenceGenerationExceptions.generateResolveSequencePathException(
						folderPathRoot, sequencePath));

		String curSequencePath = getCurrentSequencePath(ctx.getSeqDate());
		AtomicInteger ctxNextSeq = ctx.getNextSeq();

		Optional.of(ctxNextSeq).ifPresentOrElse(atomicInt -> {
			boolean done = false;
			while (!done) {
				int last = atomicInt.get();
				int valueToGenerate = getNextSequenceNumber(curSequencePath, root);

				String nextSeqFullPath = Optional.of(valueToGenerate)
						.map(nextSeq -> curSequencePath + addSubPath(String.valueOf(nextSeq)))
						.orElse(StringUtils.EMPTY);
				try {
					Files.createFile(root.resolve(nextSeqFullPath));
				} catch (IOException e) {
					continue;
				}

				done = atomicInt.compareAndSet(last, valueToGenerate);
			}
		}
		, () -> {
			throw SequenceGenerationExceptions.generateNextSequenceNotPresentException(ctxNextSeq);
		});

		Object value = Optional.ofNullable(options.getExpression())
				.map(expressionEvaluations::parse)
				.map(eval -> eval.getValue(ctx))
				.orElse(StringUtils.leftPad(String.valueOf(ctxNextSeq.get()), 3, '0'));

		return messageSupport.getMessageUsingProperties(
				options.getPatternOfFormat(), ctx, Map.of("value", value));

	}

	private String getCurrentSequencePath(String seqDate) {
		return String.format("%s%s%s%s", nullAsEmpty(options.getBasePath()), nullAsEmpty(options.getSenderId()),
				nullAsEmpty(options.getTypeId()), nullAsEmpty(addSubPath(seqDate)));
	}

	private String nullAsEmpty(String txt) {
		return Optional.ofNullable(txt)
				.map(Objects::toString)
				.orElse(StringUtils.EMPTY);
	}

	private Integer getNextSequenceNumber(String path, Path root) {
		if (StringUtils.isBlank(path)) {
			return 1;
		}

		File folder = root.resolve(path).toFile();

		if (!folder.exists()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (IOException e) {
                throw SequenceGenerationExceptions.generateCreatingDirectoryException(root, path);
            }
        }

		try (Stream<Path> fileStream = Files.list(folder.toPath())) {
			Optional<Path> maxPath = fileStream.map(Path::getFileName)
					.max((lp, rp) -> compare(lp.toString(), rp.toString()));

			return maxPath
					.map(Path::toString)
					.map(Integer::parseInt)
					.map(i -> i + 1)
					.orElse(1);
		}
		catch (Exception e) {
			return 1;
		}

	}

	private int compare(String l, String r) {
		return Integer.compare(Integer.parseInt(l), Integer.parseInt(r));
	}
	
	private String addSubPath(String subPath) {
		return subPath.endsWith(File.separator) ? 
				subPath : File.separator.concat(subPath);
	}

}
