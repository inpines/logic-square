package org.dotspace.oofp.utils.functional.monad;

import lombok.extern.slf4j.Slf4j;
import org.dotspace.oofp.utils.functional.Casters;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public final class Task<T> {

    public static final String TASK_WAS_CANCELLED = "Task was cancelled";
    private final Supplier<CompletableFuture<T>> supplier;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    private Task(Supplier<CompletableFuture<T>> supplier) {
        this.supplier = supplier;
    }

    public static <T> Task<T> just(T value) {
        return new Task<>(() -> CompletableFuture.completedFuture(value));
    }

    public static <T> Task<T> action(Supplier<T> delayed) {
        return new Task<>(() -> CompletableFuture.supplyAsync(() -> {
            if (Thread.currentThread().isInterrupted()) throw new CancellationException(TASK_WAS_CANCELLED);
            return delayed.get();
        }));
    }

    public static <T> Task<T> actionAsync(Supplier<CompletableFuture<T>> delayedAsync) {
        return new Task<>(delayedAsync);
    }

    public <R> Task<R> flatMap(Function<T, Task<R>> f) {
        return new Task<>(() -> supplier.get().thenCompose(t -> {
            if (cancelled.get()) throw new CancellationException(TASK_WAS_CANCELLED);
            return (f.apply(t)).supplier.get();
        }));
    }

    public <R> Task<R> wrap(R value) {
        return Task.just(value);
    }

    public CompletableFuture<T> run() {
        if (cancelled.get()) {
            CompletableFuture<T> cf = new CompletableFuture<>();
            cf.completeExceptionally(new CancellationException(TASK_WAS_CANCELLED));
            return cf;
        }
        return supplier.get();
    }

    public T await() throws InterruptedException, ExecutionException {
        return run().get();
    }

    public Task<T> onError(Function<Throwable, T> fallback) {
        return new Task<>(() -> supplier.get().exceptionally(fallback));
    }

    public Task<T> onErrorResume(Function<Throwable, Task<T>> fallback, Consumer<Exception> exceptionHandler) {
        return new Task<>(() -> supplier.get().handle((result, ex) -> {
            if (ex != null) {
                try {
                    return fallback.apply(ex).run().get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("exception", e);
                    exceptionHandler.accept(e);
                    Thread.currentThread().interrupt();
                    return null;
                }
            } else {
                return result;
            }
        }));
    }

    public Task<T> withTimeout(long timeout, TimeUnit unit) {
        return new Task<>(() -> {
            CompletableFuture<T> original = supplier.get();
            CompletableFuture<T> timeoutFuture = failAfter(timeout, unit);
            return CompletableFuture.anyOf(original, timeoutFuture).thenApply(obj -> {
                if (obj instanceof TimeoutException e) {
                    throw new CompletionException(e);
                }
                return Maybe.just(obj).map(Casters.<T>cast())
                        .orElse(null);
            });
        });
    }

    private static <T> CompletableFuture<T> failAfter(long timeout, TimeUnit unit) {
        CompletableFuture<T> promise = new CompletableFuture<>();
        Executors.newSingleThreadScheduledExecutor().schedule(() ->
                promise.completeExceptionally(new TimeoutException("Task timed out")), timeout, unit);
        return promise;
    }

    public void cancel() {
        cancelled.set(true);
    }

}