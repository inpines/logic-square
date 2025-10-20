package org.dotspace.oofp.support.dsl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.utils.dsl.StepContext;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.dotspace.oofp.utils.violation.joinable.Violations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BehaviorStepTest {

    // --- 小工具：建立初始 StepContext 與讀 payload ---
    private static <T> StepContext<T> ctx(T payload) {
        return StepContext.<T>builder()
                .withPayload(payload)
                .withViolations(Violations.empty())
                .build();
    }

    private static <T> T payloadOf(Validation<Violations, StepContext<T>> v) {
        return v.fold(err -> null, StepContext::getPayload);
    }

    // -------------------- of / supply --------------------

    @Test
    @DisplayName("of：應包裝 Function 並正確執行")
    void testOf() {
        BehaviorStep<Integer> step = BehaviorStep.of(Validation::valid);
        var out = step.apply(ctx(5));
        assertEquals(5, payloadOf(out));
    }

    @Test
    @DisplayName("supply：應延遲取得 Step，未執行前不調用 supplier.get()")
    void testSupply_isLazy() {
        AtomicBoolean called = new AtomicBoolean(false);
        BehaviorStep<Integer> lazy = BehaviorStep.supply(() -> {
            called.set(true);
            return Validation::valid;
        });

        assertFalse(called.get(), "尚未執行時不應呼叫 supplier");
        var out = lazy.apply(ctx(1));
        assertTrue(called.get(), "執行時才呼叫 supplier");
        assertEquals(1, payloadOf(out));
    }

    // -------------------- chain --------------------

    @Test
    @DisplayName("chain：依序串接步驟，全部成功時依序生效")
    void testChain_successFlow() {
        // 兩個 mapper：+1、再 *2
        BehaviorStep<Integer> plusOne = BehaviorStep.<Integer>of(Validation::valid)
                .andThenMapper(m -> StepContext.<Integer>builder()
                        .withPayload(m.getPayload() + 1)
                        .withViolations(Violations.empty())
                        .build());

        BehaviorStep<Integer> timesTwo = BehaviorStep.<Integer>of(Validation::valid)
                .andThenMapper(m -> StepContext.<Integer>builder()
                        .withPayload(m.getPayload() * 2)
                        .withViolations(Violations.empty())
                        .build());

        BehaviorStep<Integer> chained = BehaviorStep.chain(List.of(plusOne, timesTwo));

        var out = chained.apply(ctx(3)); // (3 + 1) * 2 = 8
        assertEquals(8, payloadOf(out));
    }

    @Test
    @DisplayName("chain：若前一步 Invalid，後續步驟不應被執行（flatMap 短路）")
    void testChain_shortCircuitOnInvalid() {
        BehaviorStep<Integer> invalidFirst = in -> Validation.invalid(in.withViolation(Violations.empty()));

        @SuppressWarnings("unchecked")
        BehaviorStep<Integer> neverCalled = mock(BehaviorStep.class);
        when(neverCalled.apply(any())).thenAnswer(inv -> Validation.valid(inv.getArgument(0)));

        BehaviorStep<Integer> chained = BehaviorStep.chain(List.of(invalidFirst, neverCalled));

        var out = chained.apply(ctx(10));
        out.fold(err -> err, ok -> fail("應為 Invalid"));

        verify(neverCalled, never()).apply(any());
    }

    // -------------------- when --------------------

    @Test
    @DisplayName("when：條件成立才執行 step，否則回傳 Valid(input)")
    void testWhen_conditionallyRun() {
        var inc = BehaviorStep.<Integer>of(Validation::valid)
                .andThenMapper(m -> StepContext.<Integer>builder()
                        .withPayload(m.getPayload() + 1)
                        .withViolations(Violations.empty())
                        .build());

        BehaviorStep<Integer> guarded =
                BehaviorStep.when(sc -> sc.getPayload() > 0, inc);

        assertEquals(2, payloadOf(guarded.apply(ctx(1))));
        assertEquals(0, payloadOf(guarded.apply(ctx(0)))); // 未執行 inc
    }

    // -------------------- andThen / andThenMapper --------------------

    @Test
    @DisplayName("andThen：前一步成功才會接著執行下一步")
    void testAndThen() {
        BehaviorStep<Integer> base = BehaviorStep.of(Validation::valid);
        BehaviorStep<Integer> step = base.andThenStep(in -> Validation.valid(
                StepContext.<Integer>builder()
                        .withPayload(in.getPayload() + 3)
                        .withViolations(Violations.empty())
                        .build()));

        assertEquals(8, payloadOf(step.apply(ctx(5))));
    }

    @Test
    @DisplayName("andThenMapper：成功後 map StepContext")
    void testAndThenMapper() {
        UnaryOperator<StepContext<Integer>> mapper = sc ->
                StepContext.<Integer>builder()
                        .withPayload(sc.getPayload() * 10)
                        .withViolations(Violations.empty())
                        .build();

        BehaviorStep<Integer> step = BehaviorStep.<Integer>of(Validation::valid)
                .andThenMapper(mapper);

        assertEquals(50, payloadOf(step.apply(ctx(5))));
    }

    // -------------------- filter --------------------

    @Test
    @DisplayName("filter：predicate 為 true -> 保持 Valid；否則 -> Invalid 並附帶 violation")
    void testFilter() {
        AtomicInteger tested = new AtomicInteger();
        var step = BehaviorStep.<Integer>of(Validation::valid)
                .filter(v -> {
                    tested.incrementAndGet();
                    return v >= 10;
                }, v -> Violations.empty()); // 測試用，回空違規集合也可

        var ok = step.apply(ctx(10));
        ok.fold(err -> fail("應為 Valid"), okv -> okv);

        var bad = step.apply(ctx(3));
        bad.fold(err -> err, okv -> fail("應為 Invalid"));

        assertEquals(2, tested.get(), "兩次呼叫 predicate");
    }

    // -------------------- peek / peekOnError --------------------

    @Test
    @DisplayName("peek：僅在成功時執行 observer")
    void testPeek() {
        AtomicBoolean called = new AtomicBoolean(false);
        var step = BehaviorStep.<Integer>of(Validation::valid)
                .peek(sc -> called.set(true));

        step.apply(ctx(1));
        assertTrue(called.get(), "成功時應呼叫 observer");

        called.set(false);
        BehaviorStep<Integer> fail = in -> Validation.invalid(in.withViolation(Violations.empty()));
        fail.peek(sc -> called.set(true)).apply(ctx(2));
        assertFalse(called.get(), "失敗時不應呼叫 observer");
    }

    @Test
    @DisplayName("peekOnError：僅在失敗時執行 handler")
    void testPeekOnError() {
        AtomicBoolean handled = new AtomicBoolean(false);
        BehaviorStep<Integer> fail = in -> Validation.invalid(in.withViolation(Violations.empty()));
        fail.peekOnError(v -> handled.set(true)).apply(ctx(0));
        assertTrue(handled.get(), "失敗時應呼叫錯誤處理");

        handled.set(false);
        BehaviorStep.<Integer>of(Validation::valid)
                .peekOnError(v -> handled.set(true))
                .apply(ctx(1));
        assertFalse(handled.get(), "成功時不應呼叫錯誤處理");
    }

    // -------------------- recover --------------------

    @Test
    @DisplayName("recover：Invalid 時以 recoveryFunction 產生替代 payload，回傳 Valid 且 violations 清空")
    void testRecover_successfulRecovery() {
        BehaviorStep<Integer> fail = in -> Validation.invalid(in.withViolation(Violations.empty()));
        BehaviorStep<Integer> recovered = fail.recover(v -> 42);

        var out = recovered.apply(ctx(7));
        assertEquals(42, payloadOf(out));
    }

    @Test
    @DisplayName("recover：recoveryFunction 回 null（Maybe empty）-> 維持 Invalid")
    void testRecover_recoveryReturnsNull_keepsInvalid() {
        BehaviorStep<Integer> fail = in -> Validation.invalid(in.withViolation(Violations.empty()));
        BehaviorStep<Integer> recovered = fail.recover(v -> null);

        var out = recovered.apply(ctx(7));
        out.fold(err -> err, ok -> fail("應維持 Invalid"));
    }
}
