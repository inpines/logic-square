package org.dotspace.oofp.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaginatorTest {

    @Test
    void testBuilderSetsLimit() {
        Paginator paginator = Paginator.builder()
                .withLimit(25)
                .build();

        assertEquals(25, paginator.getLimit());
    }
}