package org.dotspace.oofp.utils.oofp.functional.monad.number;

import org.dotspace.oofp.utils.functional.monad.number.NumberMonads;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NumberMonadsTest {

    @Test
    void getLong_validString_returnsLong() {
        assertEquals(123L, NumberMonads.getLong("123"));
        assertEquals(-456L, NumberMonads.getLong("-456"));
        assertEquals(0L, NumberMonads.getLong("0"));
    }

    @Test
    void getLong_invalidString_returnsZero() {
        assertEquals(0L, NumberMonads.getLong("abc"));
        assertEquals(0L, NumberMonads.getLong("12.34"));
        assertEquals(0L, NumberMonads.getLong(""));
        assertEquals(0L, NumberMonads.getLong("   "));
        assertEquals(0L, NumberMonads.getLong(null));
    }

    @Test
    void getInteger_validString_returnsInteger() {
        assertEquals(123, NumberMonads.getInteger("123"));
        assertEquals(-456, NumberMonads.getInteger("-456"));
        assertEquals(0, NumberMonads.getInteger("0"));
    }

    @Test
    void getInteger_invalidString_returnsZero() {
        assertEquals(0, NumberMonads.getInteger("abc"));
        assertEquals(0, NumberMonads.getInteger("12.34"));
        assertEquals(0, NumberMonads.getInteger(""));
        assertEquals(0, NumberMonads.getInteger("   "));
        assertEquals(0, NumberMonads.getInteger(null));
    }

    @Test
    void getDouble_validString_returnsDouble() {
        assertEquals(123.45, NumberMonads.getDouble("123.45"));
        assertEquals(-456.78, NumberMonads.getDouble("-456.78"));
        assertEquals(0.0, NumberMonads.getDouble("0"));
        assertEquals(123.0, NumberMonads.getDouble("123"));
    }

    @Test
    void getDouble_invalidString_returnsZero() {
        assertEquals(0.0, NumberMonads.getDouble("abc"));
        assertEquals(0.0, NumberMonads.getDouble(""));
        assertEquals(0.0, NumberMonads.getDouble("   "));
        assertEquals(0.0, NumberMonads.getDouble(null));
    }
}