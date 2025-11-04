package org.dotspace.oofp.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class PageGroupingsTest {

    @Test
    void testGroupingDelegatesToGroupingBy() {
        PageGroupings pageGroupings = new PageGroupings();
        List<String> data = Arrays.asList("a", "b", "c");
        Function<String, String> classifier = String::toUpperCase;

        PageGrouping<String> result = pageGroupings.grouping(data, classifier);

        assertNotNull(result);
    }
}