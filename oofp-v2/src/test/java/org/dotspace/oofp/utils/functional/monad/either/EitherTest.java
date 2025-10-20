package org.dotspace.oofp.utils.functional.monad.either;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

class EitherTest {

    @Test
    void left_createsLeftEither() {
        Either<String, Integer> either = Either.left("error");
        
        assertTrue(either.isLeft());
        assertFalse(either.isRight());
        assertEquals("error", either.getLeft());
    }

    @Test
    void right_createsRightEither() {
        Either<String, Integer> either = Either.right(42);
        
        assertFalse(either.isLeft());
        assertTrue(either.isRight());
        assertEquals(42, either.getRight());
    }

    @Test
    void isRight_withRightValue_returnsTrue() {
        Either<String, Integer> either = Either.right(42);
        assertTrue(either.isRight());
    }

    @Test
    void isRight_withLeftValue_returnsFalse() {
        Either<String, Integer> either = Either.left("error");
        assertFalse(either.isRight());
    }

    @Test
    void isLeft_withLeftValue_returnsTrue() {
        Either<String, Integer> either = Either.left("error");
        assertTrue(either.isLeft());
    }

    @Test
    void isLeft_withRightValue_returnsFalse() {
        Either<String, Integer> either = Either.right(42);
        assertFalse(either.isLeft());
    }

    @Test
    void getRight_withRightValue_returnsValue() {
        Either<String, Integer> either = Either.right(42);
        assertEquals(42, either.getRight());
    }

    @Test
    void getRight_withLeftValue_throwsException() {
        Either<String, Integer> either = Either.left("error");
        assertThrows(IllegalStateException.class, either::getRight);
    }

    @Test
    void getLeft_withLeftValue_returnsValue() {
        Either<String, Integer> either = Either.left("error");
        assertEquals("error", either.getLeft());
    }

    @Test
    void getLeft_withRightValue_throwsException() {
        Either<String, Integer> either = Either.right(42);
        assertThrows(IllegalStateException.class, either::getLeft);
    }

    @Test
    void stream_withRightValue_returnsStreamWithValue() {
        Either<String, Integer> either = Either.right(42);
        Stream<Integer> stream = either.stream();
        assertEquals(1, stream.count());
    }

    @Test
    void stream_withLeftValue_returnsEmptyStream() {
        Either<String, Integer> either = Either.left("error");
        Stream<Integer> stream = either.stream();
        assertEquals(0, stream.count());
    }

    @Test
    void fold_withRightValue_appliesRightFunction() {
        Either<String, Integer> either = Either.right(42);
        String result = either.fold(
            left -> "Error: " + left,
            right -> "Value: " + right
        );
        assertEquals("Value: 42", result);
    }

    @Test
    void fold_withLeftValue_appliesLeftFunction() {
        Either<String, Integer> either = Either.left("error");
        String result = either.fold(
            left -> "Error: " + left,
            right -> "Value: " + right
        );
        assertEquals("Error: error", result);
    }

    @Test
    void either_withNullValues_shouldThroNullPointException() {
        assertThrows(NullPointerException.class, () -> Either.left(null));
        assertThrows(NullPointerException.class, () -> Either.right(null));
    }
}