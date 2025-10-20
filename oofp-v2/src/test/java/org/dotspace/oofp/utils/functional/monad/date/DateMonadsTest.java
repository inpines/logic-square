package org.dotspace.oofp.utils.functional.monad.date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class DateMonadsTest {

    @Test
    @DisplayName("getLocalDateTime should parse valid datetime with milliseconds")
    void testGetLocalDateTimeValidInput() {
        String validDateTime = "2023-12-25 14:30:45.123";
        LocalDateTime result = DateMonads.getLocalDateTime(validDateTime);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
        assertEquals(123000000, result.getNano());
    }

    @Test
    @DisplayName("getLocalDateTime should return null for invalid input")
    void testGetLocalDateTimeInvalidInput() {
        assertNull(DateMonads.getLocalDateTime("invalid-date"));
        assertNull(DateMonads.getLocalDateTime(""));
        assertNull(DateMonads.getLocalDateTime(null));
    }

    @Test
    @DisplayName("getLocalDateTime should handle partial datetime strings")
    void testGetLocalDateTimePartialInput() {
        String partialDateTime = "2023-12-25";
        LocalDateTime result = DateMonads.getLocalDateTime(partialDateTime);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
    }

    @Test
    @DisplayName("getLocalDateTime with supplier should use supplier when parsing fails")
    void testGetLocalDateTimeWithSupplier() {
        LocalDateTime defaultDateTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        Supplier<LocalDateTime> supplier = () -> defaultDateTime;
        
        LocalDateTime result = DateMonads.getLocalDateTime("invalid-date", supplier);
        
        assertEquals(defaultDateTime, result);
    }

    @Test
    @DisplayName("getLocalDateTime with supplier should return parsed value when valid")
    void testGetLocalDateTimeWithSupplierValidInput() {
        LocalDateTime defaultDateTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        Supplier<LocalDateTime> supplier = () -> defaultDateTime;
        String validDateTime = "2023-12-25 14:30:45.123";
        
        LocalDateTime result = DateMonads.getLocalDateTime(validDateTime, supplier);
        
        assertNotEquals(defaultDateTime, result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
    }

    @Test
    @DisplayName("getLocalDateTimeWithHundredNanoSecond should parse nanosecond precision")
    void testGetLocalDateTimeWithHundredNanoSecond() {
        String nanoDateTime = "2023-12-25 14:30:45.1234567";
        LocalDateTime result = DateMonads.getLocalDateTimeWithHundredNanoSecond(nanoDateTime);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
        assertEquals(123456700, result.getNano());
    }

    @Test
    @DisplayName("getLocalDateTimeWithHundredNanoSecond should return null for invalid input")
    void testGetLocalDateTimeWithHundredNanoSecondInvalid() {
        assertNull(DateMonads.getLocalDateTimeWithHundredNanoSecond("invalid-date"));
        assertNull(DateMonads.getLocalDateTimeWithHundredNanoSecond(null));
    }

    @Test
    @DisplayName("getLocalDate should parse date-only strings")
    void testGetLocalDate() {
        String dateOnly = "2023-12-25";
        LocalDateTime result = DateMonads.getLocalDate(dateOnly);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(25, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    @DisplayName("getLocalDate should return null for invalid input")
    void testGetLocalDateInvalid() {
        assertNull(DateMonads.getLocalDate("invalid-date"));
        assertNotNull(DateMonads.getLocalDateTimeWithMilli("2023-12-25 14:30:45"));
        assertNull(DateMonads.getLocalDate(null));
    }

    @Test
    @DisplayName("getLocalDate should handle partial date strings")
    void testGetLocalDatePartial() {
        String partialDate = "2023-12";
        LocalDateTime result = DateMonads.getLocalDate(partialDate);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(12, result.getMonthValue());
    }
}