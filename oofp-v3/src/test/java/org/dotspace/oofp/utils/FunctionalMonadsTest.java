package org.dotspace.oofp.utils;

import org.dotspace.oofp.model.dto.behaviorstep.JoinableMessage;
import org.dotspace.oofp.utils.functional.monad.either.Either;
import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.Sequence;
import org.dotspace.oofp.utils.functional.monad.Task;
import org.dotspace.oofp.utils.functional.monad.either.Try;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionalMonadsTest {

    @Test
    void testMaybeMapAndFold() {
        Maybe<String> maybe = Maybe.just("Alice");
        String result = maybe.map(String::toUpperCase).fold(s -> s, () -> "Empty");
        assertEquals("ALICE", result);
    }

    @Test
    void testMaybeEmptyFold() {
        Maybe<String> maybe = Maybe.empty();
        String result = maybe.fold(s -> s, () -> "Default");
        assertEquals("Default", result);
    }

    @Test
    void testSequenceFilterAndMap() {
        Sequence<Integer> seq = Sequence.of(new Integer[] {1, 2, 3, 4});
        Sequence<Integer> filtered = seq.filter(n -> n % 2 == 0).map(n -> n * 10);
        assertEquals(List.of(20, 40), filtered.collect(Collectors.toList()));
    }

    @Test
    void testEitherMatchRight() {
        Either<String, Integer> result = Either.right(100);
        assertEquals(100, result.fold(Function.identity(), Function.identity()));
    }

    @Test
    void testTrySuccess() {
        Try<Integer> division = Try.attempt(() -> 10 / 2);
        assertTrue(division.isSuccess());
        assertEquals(5, division.orElse(-1));
    }

    @Test
    void testTryFailure() {
        Try<Integer> failure = Try.attempt(() -> 10 / 0);
        assertTrue(failure.isFailure());
    }

    @Test
    void testValidationValid() {
        Validation<?, Integer> valid = Validation.valid(123);
        assertTrue(valid.isValid());
    }

    @Test
    void testValidationInvalid() {
        Validation<JoinableMessage, Integer> invalid = Validation.invalid(JoinableMessage.of("Invalid data"));
        assertTrue(invalid.isInvalid());
        assertEquals("Invalid data", invalid.error()
                .fold(JoinableMessage::getMessage, () -> StringUtils.EMPTY)
        );
    }

    @Test
    void testTaskExecution() throws Exception {
        Task<String> task = Task.action(() -> "completed");
        String result = task.run().get();
        assertEquals("completed", result);
    }
}
