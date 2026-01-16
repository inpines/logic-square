package org.dotspace.oofp.utils.oofp.functional.monad.text;

import org.dotspace.oofp.utils.functional.monad.Maybe;
import org.dotspace.oofp.utils.functional.monad.text.StringMonads;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StringMonadsTest {
    @Test
    void testGetString_null() {
        assertEquals(StringUtils.EMPTY, StringMonads.getString(null));
    }

    @Test
    void testGetString_empty() {
        assertEquals(StringUtils.EMPTY, StringMonads.getString(""));
    }

    @Test
    void testGetString_blank() {
        assertEquals(StringUtils.EMPTY, StringMonads.getString("   "));
    }

    @Test
    void testGetString_normal() {
        assertEquals("abc", StringMonads.getString("abc"));
    }

    @Test
    void testMaybeStringContent_null() {
        assertTrue(StringMonads.maybeStringContent(null).isEmpty());
    }

    @Test
    void testMaybeStringContent_empty() {
        assertTrue(StringMonads.maybeStringContent("").isEmpty());
    }

    @Test
    void testMaybeStringContent_blank() {
        assertTrue(StringMonads.maybeStringContent("   ").isEmpty());
    }

    @Test
    void testMaybeStringContent_normal() {
        Maybe<String> maybe = StringMonads.maybeStringContent("abc");
        assertTrue(maybe.isPresent());
        assertEquals("abc", maybe.get());
    }

    @Test
    void testGetStringWithSupplier_null() {
        Supplier<String> supplier = () -> "default";
        assertEquals("default", StringMonads.getString(null, supplier));
    }

    @Test
    void testGetStringWithSupplier_empty() {
        Supplier<String> supplier = () -> "default";
        assertEquals("default", StringMonads.getString("", supplier));
    }

    @Test
    void testGetStringWithSupplier_blank() {
        Supplier<String> supplier = () -> "default";
        assertEquals("default", StringMonads.getString("   ", supplier));
    }

    @Test
    void testGetStringWithSupplier_normal() {
        Supplier<String> supplier = () -> "default";
        assertEquals("abc", StringMonads.getString("abc", supplier));
    }

    @Test
    void testGetStringWithSupplier_supplierIsNull_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> StringMonads.getString(null, null));
    }

    @Test
    void testGetStringWithSupplier_blankWithSupplierIsNull_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> StringMonads.getString("   ", null));
    }

    @Test
    void testGetTrimmedString_null() {
        assertEquals(StringUtils.EMPTY, StringMonads.getTrimmedString(null));
    }

    @Test
    void testGetTrimmedString_empty() {
        assertEquals(StringUtils.EMPTY, StringMonads.getTrimmedString(""));
    }

    @Test
    void testGetTrimmedString_blank() {
        assertEquals(StringUtils.EMPTY, StringMonads.getTrimmedString("   "));
    }

    @Test
    void testGetTrimmedString_normal() {
        assertEquals("abc", StringMonads.getTrimmedString("  abc  "));
    }

    @Test
    void testGetTrimmedString_trimmedIsEmpty() {
        assertEquals(StringUtils.EMPTY, StringMonads.getTrimmedString("   "));
        assertEquals(StringUtils.EMPTY, StringMonads.getTrimmedString(" \t \n "));
    }

    @Test
    void testMaybeStringContent_specialWhitespace() {
        assertTrue(StringMonads.maybeStringContent("\t\n").isEmpty());
    }

    @Test
    void testUtilityClassConstructor() throws Exception {
        var constructor = StringMonads.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(Exception.class, constructor::newInstance);
    }
}