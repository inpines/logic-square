package org.dotspace.oofp.utils.functional.monad.either;

import org.dotspace.oofp.utils.functional.monad.Task;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class TaskExtensionsTest {

    @Test
    void asEither_shouldWrapSuccess() throws Exception {
        // given
        Task<String> original = Task.action(() -> "hello");

        // when
        Task<Try<String>> wrapped = TaskExtensions.asEither(original);
        Try<String> result = wrapped.await();

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.orElse(null))
                .isEqualTo("hello");
    }

    @Test
    void asEither_shouldWrapFailure() throws Exception {
        // given
        Task<String> original = Task.action(() -> {
            throw new IllegalStateException("boom");
        });

        // when
        Task<Try<String>> wrapped = TaskExtensions.asEither(original);
        Try<String> result = wrapped.await();

        // then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getLeft().getCause()).isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void asEither_shouldHandleAsyncTask() throws Exception {
        // given
        Supplier<CompletableFuture<String>> futureSupplier = () -> CompletableFuture.supplyAsync(
                () -> "async-value");
        Task<String> asyncTask = Task.actionAsync(futureSupplier);

        // when
        Task<Try<String>> wrapped = TaskExtensions.asEither(asyncTask);
        Try<String> result = wrapped.await();

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.orElse(null)).isEqualTo("async-value");
    }
}
