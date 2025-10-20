package org.dotspace.oofp.support.expression.transform;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TransitionsTest {

    @Test
    void testFilterByWriterPrefix() {
        Map<String, String> rules = Map.of(
                "name", "metaName",
                "age", "metaAge",
                "accountNo", "acctNo",
                "amount", "totalAmt"
        );

        Transitions all = Transitions.from(rules);
        Transitions metaOnly = all.filter(m -> m.writerExpr().startsWith("meta"));

        Set<String> expectedWriterFields = Set.of("metaName", "metaAge");
        Set<String> actualWriterFields = metaOnly.allTargetFields();

        assertEquals(expectedWriterFields, actualWriterFields);
        assertEquals(2, metaOnly.stream().count());
    }

    @Test
    void testEmptyFilter() {
        Map<String, String> rules = Map.of(
                "name", "metaName",
                "age", "metaAge"
        );

        Transitions all = Transitions.from(rules);
        Transitions none = all.filter(m -> m.writerExpr().startsWith("foo"));

        assertEquals(0, none.stream().count());
    }

    @Test
    void testMergeTransitions() {
        Transitions t1 = Transitions.from(Map.of(
                "name", "metaName",
                "age", "metaAge"
        ));

        Transitions t2 = Transitions.from(Map.of(
                "accountNo", "acctNo",
                "amount", "totalAmt"
        ));

        Transitions merged = t1.merge(t2, TransitionOptions.defaultOptions(), RuntimeException::new);

        Set<String> expectedTargetFields = Set.of("metaName", "metaAge", "acctNo", "totalAmt");

        assertEquals(4, merged.stream().count());
        assertEquals(expectedTargetFields, merged.allTargetFields());
    }

    @Test
    void testMergeWithOverwriting() {
        Transitions base = Transitions.from(Map.of(
                "a", "field1",
                "b", "field2"
        ));

        Transitions other = Transitions.from(Map.of(
                "c", "field2", // 衝突
                "d", "field3"
        ));

        TransitionOptions options = TransitionOptions.builder()
                .overwriting(true)
                .build();

        Transitions merged = base.merge(other, options, RuntimeException::new);

        List<TransformMapping> mergedList = merged.stream().toList();
        assertEquals(3, mergedList.size());
        assertTrue(mergedList.stream().anyMatch(m -> m.writerExpr().equals("field2")
                && m.readerExpr().equals("c")));
    }

    @Test
    void testMergeWithoutOverwritingShouldFail() {
        Transitions base = Transitions.from(Map.of(
                "a", "field1",
                "b", "field2"
        ));

        Transitions other = Transitions.from(Map.of(
                "c", "field2", // 衝突
                "d", "field3"
        ));

        TransitionOptions options = TransitionOptions.builder()
                .overwriting(false)
                .build();

        assertThrows(RuntimeException.class, () -> base.merge(other, options, RuntimeException::new));
    }

}
