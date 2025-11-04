package org.dotspace.oofp.support.orm.specification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CriteriaOrderTest {

    @Test
    void asc_setsTypeToAscendingAndPreservesName() {
        CriteriaOrder order = CriteriaOrder.asc("myField");
        assertEquals("ascending", order.getType());
        assertEquals("myField", order.getName());
    }

    @Test
    void desc_setsTypeToDescendingAndPreservesName() {
        CriteriaOrder order = CriteriaOrder.desc("otherField");
        assertEquals("descending", order.getType());
        assertEquals("otherField", order.getName());
    }

    @Test
    void asc_allowsNullName() {
        CriteriaOrder order = CriteriaOrder.asc(null);
        assertEquals("ascending", order.getType());
        assertNull(order.getName());
    }

    @Test
    void desc_allowsNullName() {
        CriteriaOrder order = CriteriaOrder.desc(null);
        assertEquals("descending", order.getType());
        assertNull(order.getName());
    }
}