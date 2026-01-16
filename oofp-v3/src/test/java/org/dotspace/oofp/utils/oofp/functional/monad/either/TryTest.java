package org.dotspace.oofp.utils.oofp.functional.monad.either;

import org.dotspace.oofp.utils.functional.monad.either.Try;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

class TryTest {

    @Test
    void attempt_withSuccessfulCallable_returnsSuccess() {
        Callable<String> callable = () -> "success";
        Try<String> result = Try.attempt(callable);
        
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("success", result.getRight());
    }

    @Test
    void attempt_withFailingCallable_returnsFailure() {
        RuntimeException exception = new RuntimeException("test error");
        Callable<String> callable = () -> { throw exception; };
        Try<String> result = Try.attempt(callable);
        
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals(exception, result.getLeft());
    }

    @Test
    void success_createsSuccessfulTry() {
        Try<String> result = Try.success("value");
        
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("value", result.getRight());
    }

    @Test
    void failed_createsFailedTry() {
        RuntimeException exception = new RuntimeException("error");
        Try<String> result = Try.failed(exception);
        
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals(exception, result.getLeft());
    }

    @Test
    void orElse_withSuccess_returnsOriginalValue() {
        Try<String> success = Try.success("original");
        assertEquals("original", success.orElse("fallback"));
    }

    @Test
    void orElse_withFailure_returnsFallbackValue() {
        Try<String> failure = Try.failed(new RuntimeException());
        assertEquals("fallback", failure.orElse("fallback"));
    }

    @Test
    void stream_withSuccess_returnsStreamWithValue() {
        Try<String> success = Try.success("value");
        Stream<String> stream = success.stream();
        assertEquals(1, stream.count());
    }

    @Test
    void stream_withFailure_returnsEmptyStream() {
        Try<String> failure = Try.failed(new RuntimeException());
        Stream<String> stream = failure.stream();
        assertEquals(0, stream.count());
    }

    @Test
    void flatMap_withSuccess_appliesFunction() {
        Try<String> success = Try.success("5");
        Try<Integer> result = success.flatMap(s -> Try.success(Integer.parseInt(s)));
        
        assertTrue(result.isSuccess());
        assertEquals(5, result.getRight());
    }

    @Test
    void flatMap_withFailure_propagatesFailure() {
        RuntimeException exception = new RuntimeException("error");
        Try<String> failure = Try.failed(exception);
        Try<Integer> result = failure.flatMap(s -> Try.success(Integer.parseInt(s)));
        
        assertTrue(result.isFailure());
        assertEquals(exception, result.getLeft());
    }

    @Test
    void map_withSuccess_appliesMapper() {
        Try<String> success = Try.success("5");
        Try<Integer> result = success.map(Integer::parseInt);
        
        assertTrue(result.isSuccess());
        assertEquals(5, result.getRight());
    }

    @Test
    void map_withFailure_propagatesFailure() {
        RuntimeException exception = new RuntimeException("error");
        Try<String> failure = Try.failed(exception);
        Try<Integer> result = failure.map(Integer::parseInt);
        
        assertTrue(result.isFailure());
        assertEquals(exception, result.getLeft());
    }

    @Test
    void map_withExceptionInMapper_returnsFailure() {
        Try<String> success = Try.success("invalid");
        Try<Integer> result = success.map(Integer::parseInt);
        
        assertTrue(result.isFailure());
        assertInstanceOf(NumberFormatException.class, result.getLeft());
    }
}