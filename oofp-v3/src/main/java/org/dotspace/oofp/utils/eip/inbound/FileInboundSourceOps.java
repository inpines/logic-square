package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.utils.eip.AttrKey;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@UtilityClass
public class FileInboundSourceOps {

    class Inbound<T> {
        StepContext<T> context;

        @Getter
        File file;

        Inbound(StepContext<T> stepContext) {
            this.context = stepContext;
        }

        Inbound<T> resolve(File file) {
            if (null == file) {
                return this;
            }

            this.file = file;
            context = context.withAttribute(FLAT_READ_INBOUND_FILE, this.file);
            return this;
        }
    }

    public static final String FLAT_READ_INBOUND_FILE = "flat-read.inbound-file";

    public <T, R> BehaviorStep<T> read(
            Function<T, File> fileResolver, Function<File, R> reader, AttrKey<R> attrKey) {
        return flatRead(sc -> Validation
                .<Violations, StepContext<T>>valid(sc)
                .map(StepContext::getPayload)
                .map(fileResolver), f -> Validation.<Violations, File>valid(f).map(reader), attrKey);
    }

    public <T, R> BehaviorStep<T> flatRead(
            Function<StepContext<T>, Validation<Violations, File>> fileResolver,
            Function<File, Validation<Violations, R>> reader,
            AttrKey<R> attrKey) {

        return stepContext -> Validation.<Violations, StepContext<T>>valid(stepContext)
                    .peek(sc -> debug("flat-read.step-context={}", sc))
                    .flatMap(fileResolver)
                    .peek(f -> debug("flatRead.inboundFile={}", f))
                    .map(f -> new Inbound<>(stepContext).resolve(f))
                    .flatMap(inb -> {
                        StepContext<T> resolved = inb.context;

                        return Maybe.given(inb.getFile())
                                .toValidation(Violations.violate("flat-read.inbound-file.invalid",
                                        "invalid inboundFile"))
                                .flatMap(reader)
                                .peek(r -> debug("flatRead.read-result={}", summarize(r)))
                                .map(r -> resolved.withAttribute(attrKey.name(), r))
                                .peek(sc -> debugStepContextAttributeValue(attrKey, sc));
                    })
                    .peekError(violations -> error(
                            violations.collectMessages()));
    }

    private void debug(String message, Object... data) {
        log.debug(message, data);
    }

    private <T, R> void debugStepContextAttributeValue(
            AttrKey<R> attrKey, StepContext<T> sc) {
        sc.getAttribute(attrKey.name(), Objects::toString)
                .match(value -> debug(
                        "flatRead.attribute {}:{}", attrKey.name(), value)
                );
    }

    private Object summarize(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof byte[] b) {
            return "byte[" + b.length + "]";
        }
        if (v instanceof CharSequence s && s.length() > 500) {
            return s.subSequence(0, 500) + "...(" + s.length() + ")";
        }
        return v;
    }

    private void error(Object... data) {
        log.error("flatRead.violations: 讀取 payload 失敗 -> {}", data);
    }

}
