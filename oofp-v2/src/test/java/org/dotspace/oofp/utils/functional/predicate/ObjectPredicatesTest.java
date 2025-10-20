package org.dotspace.oofp.utils.functional.predicate;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ObjectPredicatesTest {

    @Test
    void testIsNotEmpty_Object_WithNull() {
        assertFalse(ObjectPredicates.isNotEmpty((Object) null));
    }

    @Test
    void testIsNotEmpty_Object_WithEmptyString() {
        assertFalse(ObjectPredicates.isNotEmpty(""));
    }

    @Test
    void testIsNotEmpty_Object_WithNonEmptyString() {
        assertTrue(ObjectPredicates.isNotEmpty("test"));
    }

    @Test
    void testIsNotEmpty_Object_WithEmptyArray() {
        assertFalse(ObjectPredicates.isNotEmpty(new String[0]));
    }

    @Test
    void testIsNotEmpty_Object_WithNonEmptyArray() {
        assertTrue(ObjectPredicates.isNotEmpty(new String[]{"test"}));
    }

    @Test
    void testIsNotEmpty_Object_WithEmptyList() {
        assertFalse(ObjectPredicates.isNotEmpty(Collections.emptyList()));
    }

    @Test
    void testIsNotEmpty_Object_WithNonEmptyList() {
        assertTrue(ObjectPredicates.isNotEmpty(Arrays.asList("test")));
    }

    @Test
    void testIsNotEmpty_Array_WithNull() {
        assertFalse(ObjectPredicates.isNotEmpty((Object[]) null));
    }

    @Test
    void testIsNotEmpty_Array_WithEmptyArray() {
        assertFalse(ObjectPredicates.isNotEmpty(new Object[0]));
    }

    @Test
    void testIsNotEmpty_Array_WithSingleElement() {
        assertTrue(ObjectPredicates.isNotEmpty(new Object[]{"test"}));
    }

    @Test
    void testIsNotEmpty_Array_WithMultipleElements() {
        assertTrue(ObjectPredicates.isNotEmpty(new Object[]{"test1", "test2"}));
    }

    @Test
    void testIsExistElements_WithNull() {
        assertFalse(ObjectPredicates.isExistElements(null));
    }

    @Test
    void testIsExistElements_WithEmptyList() {
        assertFalse(ObjectPredicates.isExistElements(new ArrayList<>()));
    }

    @Test
    void testIsExistElements_WithSingleElement() {
        assertTrue(ObjectPredicates.isExistElements(Arrays.asList("test")));
    }

    @Test
    void testIsExistElements_WithMultipleElements() {
        assertTrue(ObjectPredicates.isExistElements(Arrays.asList("test1", "test2")));
    }
}