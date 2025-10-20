package org.dotspace.oofp.utils.functional.monad;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testJustCreatesCompletedTask() throws Exception {
        String value = "test";
        Task<String> task = Task.just(value);
        
        assertEquals(value, task.await());
    }

    @Test
    void testActionExecutesSupplier() throws Exception {
        String expected = "computed";
        Task<String> task = Task.action(() -> expected);
        
        assertEquals(expected, task.await());
    }

    @Test
    void testActionThrowsCancellationWhenInterrupted() {
        Task<String> task = Task.action(() -> {
            Thread.currentThread().interrupt();
            throw new RuntimeException(new InterruptedException("interrupted"));
        });

        task.run();
        assertThrows(ExecutionException.class, task::await);
    }

    @Test
    void testActionAsync() throws Exception {
        String expected = "async result";
        CompletableFuture<String> future = CompletableFuture.completedFuture(expected);
        Task<String> task = Task.actionAsync(() -> future);
        
        assertEquals(expected, task.await());
    }

    @Test
    void testFlatMapChaining() throws Exception {
        Task<String> task = Task.just("hello")
            .flatMap(s -> Task.just(s + " world"));
        
        assertEquals("hello world", task.await());
    }

    @Test
    void testFlatMapWithCancelledTask() {
        Task<String> task = Task.just("test");
        task.cancel();
        
        Task<String> result = task.flatMap(s -> Task.just(s + " mapped"));
        
        assertThrows(ExecutionException.class, result::await);
    }

    @Test
    void testWrapCreatesNewTask() throws Exception {
        Task<String> original = Task.just("original");
        Task<Integer> wrapped = original.wrap(42);
        
        assertEquals(Integer.valueOf(42), wrapped.await());
    }

    @Test
    void testRunReturnsCompletableFuture() throws Exception {
        String expected = "test";
        Task<String> task = Task.just(expected);
        
        CompletableFuture<String> future = task.run();
        assertEquals(expected, future.get());
    }

    @Test
    void testRunWithCancelledTask() {
        Task<String> task = Task.just("test");
        task.cancel();
        
        CompletableFuture<String> future = task.run();
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void testAwaitBlocksUntilCompletion() throws Exception {
        Task<String> task = Task.action(() -> {
            await().atMost(2, TimeUnit.SECONDS).until(didTheThing());
            return "delayed";
        });
        
        assertEquals("delayed", task.await());
    }

    @Test
    void testOnErrorWithException() throws Exception {
        Task<String> task = Task.action(() -> {
            throw new RuntimeException("error");
        });
        
        Task<String> recovered = task.onError(throwable -> "fallback");
        assertEquals("fallback", recovered.await());
    }

    @Test
    void testOnErrorWithoutException() throws Exception {
        Task<String> task = Task.just("success");
        
        Task<String> recovered = task.onError(throwable -> "fallback");
        assertEquals("success", recovered.await());
    }

    @Test
    void testOnErrorResumeWithException() throws Exception {
        Task<String> task = Task.action(() -> {
            throw new RuntimeException("error");
        });
        
        Task<String> recovered = task.onErrorResume(throwable -> Task.just("resumed"), e -> {});
        assertEquals("resumed", recovered.await());
    }

    @Test
    void testOnErrorResumeWithoutException() throws Exception {
        Task<String> task = Task.just("success");
        
        Task<String> recovered = task.onErrorResume(throwable -> Task.just("resumed"), e -> {});
        assertEquals("success", recovered.await());
    }

    @Test
    void testOnErrorResumeWithFallbackException() {
        Task<String> task = Task.action(() -> {
            throw new RuntimeException("original error");
        });
        
        Task<String> recovered = task.onErrorResume(throwable -> {
            throw new RuntimeException("fallback error");
        }, e -> {});
        
        assertThrows(ExecutionException.class, recovered::await);
    }

    @Test
    @Timeout(3)
    void testWithTimeoutCompletesBefore() throws Exception {
        Task<String> task = Task.action(() -> {
            // 比 1 秒更短，別用三層 timeout 疊加
            LockSupport.parkNanos(500_000_000L); // 0.5s
            return "completed";
        });
        
        Task<String> timedTask = task.withTimeout(1, TimeUnit.SECONDS);
        timedTask.run();
        assertEquals("completed", timedTask.await());
    }

    private Callable<Boolean> didTheThing() {
        return new Callable<>() {
            final long startTime = System.currentTimeMillis();
            public synchronized Boolean call() {
                // check the condition that must be fulfilled...
                return System.currentTimeMillis() - startTime >= 1000;
            }
        };
    }

        @Test
    @Timeout(2)
    void testWithTimeoutTimesOut() {
        Task<String> task = Task.action(() -> {
            await().atMost(2, TimeUnit.SECONDS).until(didTheThing());
            return "should not complete";
        });
        
        Task<String> timedTask = task.withTimeout(100, TimeUnit.MILLISECONDS);
        
        assertThrows(ExecutionException.class, timedTask::await);
    }

    @Test
    void testCancelPreventsFutureExecution() {
        Task<String> task = Task.action(() -> "should not execute");
        
        task.cancel();
        
        assertThrows(CancellationException.class, task::await);
    }

    @Test
    void testMultipleFlatMapOperations() throws Exception {
        Task<Integer> task = Task.just(1)
            .flatMap(n -> Task.just(n + 1))
            .flatMap(n -> Task.just(n * 2))
            .flatMap(n -> Task.just(n + 10));
        
        assertEquals(Integer.valueOf(14), task.await());
    }

    @Test
    void testAsyncChaining() throws Exception {
        Task<String> task = Task.actionAsync(() -> 
            CompletableFuture.supplyAsync(() -> "async1"))
            .flatMap(s -> Task.actionAsync(() -> 
                CompletableFuture.supplyAsync(() -> s + "-async2")));
        
        assertEquals("async1-async2", task.await());
    }

    @Test
    void testErrorPropagationThroughChain() {
        Task<String> task = Task.just("start")
            .flatMap(s -> Task.action(() -> {
                throw new RuntimeException("chain error");
            }))
            .flatMap(s -> Task.just(s + " should not reach"));
        
        assertThrows(ExecutionException.class, task::await);
    }

    @Test
    void testConcurrentExecution() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);
        
        Task<String> task1 = Task.action(() -> {
            latch.countDown();
            return "task1";
        });
        
        Task<String> task2 = Task.action(() -> {
            latch.countDown();
            return "task2";
        });
        
        Task<String> task3 = Task.action(() -> {
            latch.countDown();
            return "task3";
        });
        
        CompletableFuture<String> future1 = task1.run();
        CompletableFuture<String> future2 = task2.run();
        CompletableFuture<String> future3 = task3.run();
        
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals("task1", future1.get());
        assertEquals("task2", future2.get());
        assertEquals("task3", future3.get());
    }

    @Test
    void testNullValueHandling() throws Exception {
        Task<String> task = Task.just(null);
        
        assertNull(task.await());
    }

    @Test
    void testExceptionInSupplier() {
        Task<String> task = Task.action(() -> {
            throw new IllegalArgumentException("test exception");
        });
        
        ExecutionException exception = assertThrows(ExecutionException.class, task::await);
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("test exception", exception.getCause().getMessage());
    }

    @Test
    void testTimeoutWithNullResult() {
        Task<String> task = Task.action(() -> null);
        Task<String> timedTask = task.withTimeout(1, TimeUnit.SECONDS);
        
        assertThrows(ExecutionException.class, timedTask::await);
    }
}