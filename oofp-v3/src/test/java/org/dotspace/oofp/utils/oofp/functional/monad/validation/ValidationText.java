package org.dotspace.oofp.utils.oofp.functional.monad.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.dotspace.oofp.utils.dsl.Joinable;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 測試專用：最小的 Joinable<E> 實作，便於驗證 merge/mergeAll
final class Err implements Joinable<Err> {
    private final List<String> messages;

    Err(String... msgs) { this.messages = new ArrayList<>(Arrays.asList(msgs)); }

    List<String> getMessages() { return messages; }

    @Override public Err join(Err other) {
        List<String> all = new ArrayList<>(this.messages);
        all.addAll(other.messages);
        return new Err(all.toArray(String[]::new));
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Err)) return false;
        return Objects.equals(messages, ((Err) o).messages);
    }

    @Override public int hashCode() { return Objects.hash(messages); }

    @Override public String toString() { return "Err" + messages; }
}

class ValidationTest {

    // ---------- valid / invalid 基本行為 ----------

    @Test @DisplayName("Valid: isValid=true, isInvalid=false, fold 走 onValid")
    void valid_basics() {
        Validation<Err, String> v = Validation.valid("ok");

        assertTrue(v.isValid());
        assertFalse(v.isInvalid());
        assertEquals("OK", v.fold(err -> "bad", String::toUpperCase));

        AtomicReference<String> got = new AtomicReference<>();
        v.get().match(got::set); // Maybe.match 執行
        assertEquals("ok", got.get());

        AtomicBoolean calledErr = new AtomicBoolean(false);
        v.error().match(e -> calledErr.set(true));
        assertFalse(calledErr.get(), "Valid 的 error 應為 empty");
    }

    @Test @DisplayName("Invalid: isValid=false, isInvalid=true, fold 走 onInvalid")
    void invalid_basics() {
        Err e = new Err("E1");
        Validation<Err, String> v = Validation.invalid(e);

        assertFalse(v.isValid());
        assertTrue(v.isInvalid());
        assertEquals("E1", v.fold(err -> err.getMessages().get(0), s -> "ok"));

        AtomicBoolean calledVal = new AtomicBoolean(false);
        v.get().match(x -> calledVal.set(true));
        assertFalse(calledVal.get(), "Invalid 的 get 應為 empty");

        AtomicReference<Err> gotErr = new AtomicReference<>();
        v.error().match(gotErr::set);
        assertEquals(e, gotErr.get());
    }

    // ---------- map / flatMap ----------

    @Test @DisplayName("Valid.map：對值做映射；Invalid.map：保持 Invalid")
    void map_behaviors() {
        Validation<Err, Integer> v = Validation.valid(3);
        Validation<Err, String> v2 = v.map(i -> "n=" + i);

        assertTrue(v2.isValid());
        assertEquals("n=3", v2.fold(Err::toString, s -> s));

        Validation<Err, Integer> iv = Validation.invalid(new Err("X"));
        assertTrue(iv.map(i -> i + 1).isInvalid(), "Invalid.map 應保持 Invalid");
    }

    @Test @DisplayName("Valid.flatMap：依映射結果回 Valid/Invalid；Invalid.flatMap：保持 Invalid")
    void flatMap_behaviors() {
        Validation<Err, Integer> v = Validation.valid(10);
        var ok = v.flatMap(i -> Validation.valid(i * 2));
        var bad = v.flatMap(i -> Validation.invalid(new Err("BAD")));

        assertTrue(ok.isValid());
        assertEquals(20, ok.fold(Err::toString, x -> x));

        assertTrue(bad.isInvalid());
        assertEquals(List.of("BAD"), bad.fold(Err::getMessages, x -> List.of()));
    }

    // ---------- peek / peekError ----------

    @Test @DisplayName("peek：只在 Valid 執行 onSuccess；peekError：只在 Invalid 執行 onError")
    void peek_and_peekError() {
        AtomicBoolean seenOk = new AtomicBoolean(false);
        AtomicBoolean seenErr = new AtomicBoolean(false);

        Validation<Err, String> v = Validation.valid("hey");
        v.peek(s -> seenOk.set(true))
                .peekError(e -> seenErr.set(true));
        assertTrue(seenOk.get());
        assertFalse(seenErr.get());

        seenOk.set(false);
        seenErr.set(false);

        Validation<Err, String> iv = Validation.invalid(new Err("E"));
        iv.peek(s -> seenOk.set(true))
                .peekError(e -> seenErr.set(true));
        assertFalse(seenOk.get());
        assertTrue(seenErr.get());
    }

    // ---------- filter ----------

    @Test @DisplayName("filter：Valid + predicate=true => 保持 Valid；false => 變 Invalid(onFailure)")
    void filter_valid_path() {
        Validation<Err, Integer> v = Validation.valid(5);
        var keep = v.filter(n -> n > 0, new Err("NEG"));
        assertTrue(keep.isValid());
        assertEquals(5, keep.fold(Err::toString, x -> x));

        var toInvalid = v.filter(n -> n < 0, new Err("NEG"));
        assertTrue(toInvalid.isInvalid());
        assertEquals(List.of("NEG"), toInvalid.fold(Err::getMessages, x -> List.of()));
    }

    @Test @DisplayName("filter：原本是 Invalid => 原樣返回")
    void filter_invalid_kept() {
        Validation<Err, Integer> iv = Validation.invalid(new Err("E"));
        var out = iv.filter(n -> true, new Err("NEW"));
        assertSame(iv, out, "Invalid 應直接返回 this");
    }

    // ---------- merge ----------

    @Test @DisplayName("merge：兩個 Valid => combiner 應被套用")
    void merge_two_valid() {
        var va = Validation.<Err, Integer>valid(2);
        var vb = Validation.<Err, Integer>valid(40);

        var sum = Validation.merge(va, vb, Integer::sum);
        assertTrue(sum.isValid());
        assertEquals(42, sum.fold(Err::toString, x -> x));
    }

    @Test @DisplayName("merge：一個 Invalid => 回該錯；兩個 Invalid => 錯誤 join")
    void merge_with_invalids() {
        var e1 = new Err("A");
        var e2 = new Err("B");

        var vaBad = Validation.<Err, Integer>invalid(e1);
        var vbOk  = Validation.<Err, Integer>valid(1);
        var out1  = Validation.merge(vaBad, vbOk, Integer::sum);
        assertTrue(out1.isInvalid());
        assertEquals(List.of("A"), out1.fold(Err::getMessages, x -> List.of()));

        var vaOk  = Validation.<Err, Integer>valid(1);
        var vbBad = Validation.<Err, Integer>invalid(e2);
        var out2  = Validation.merge(vaOk, vbBad, Integer::sum);
        assertTrue(out2.isInvalid());
        assertEquals(List.of("B"), out2.fold(Err::getMessages, x -> List.of()));

        var vaBad2 = Validation.<Err, Integer>invalid(e1);
        var vbBad2 = Validation.<Err, Integer>invalid(e2);
        var outBoth = Validation.merge(vaBad2, vbBad2, Integer::sum);
        assertTrue(outBoth.isInvalid());
        assertEquals(List.of("A", "B"), outBoth.fold(Err::getMessages, x -> List.of()),
                "兩個錯誤應以 e1.join(e2) 合併");
    }

    // ---------- mergeAll ----------

    @Test @DisplayName("mergeAll：全部 Valid => 回 Map（key→value）")
    void mergeAll_all_valid() {
        Map<String, Validation<Err, Object>> m = new LinkedHashMap<>();
        m.put("a", Validation.valid(1));
        m.put("b", Validation.valid("x"));

        var res = Validation.mergeAll(m);
        assertTrue(res.isValid());

        Map<String, Object> vals = res.fold(
                err -> Map.of("err", err.toString()), x -> x);
        assertEquals(2, vals.size());
        assertEquals(1, vals.get("a"));
        assertEquals("x", vals.get("b"));
    }

    @Test @DisplayName("mergeAll：含 Invalid => 聚合錯誤（按 join 合併）")
    void mergeAll_with_invalids() {
        Map<String, Validation<Err, Object>> m = new LinkedHashMap<>();
        m.put("a", Validation.valid(1));
        m.put("b", Validation.invalid(new Err("E1")));
        m.put("c", Validation.invalid(new Err("E2")));

        var res = Validation.mergeAll(m);
        assertTrue(res.isInvalid());
        assertEquals(List.of("E1", "E2"),
                res.fold(Err::getMessages, ok -> List.of()));
    }
}
