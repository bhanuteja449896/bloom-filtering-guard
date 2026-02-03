package com.bloomguard.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void isValidCardNumber_shouldReturnTrueForValidVisaCard() {
        assertTrue(ValidationUtil.isValidCardNumber("4111111111111111"));
    }

    @Test
    void isValidCardNumber_shouldReturnFalseForInvalidLuhn() {
        assertFalse(ValidationUtil.isValidCardNumber("4111111111111112"));
    }

    @Test
    void isValidCardNumber_shouldReturnFalseForTooShort() {
        assertFalse(ValidationUtil.isValidCardNumber("411111111111"));
    }

    @Test
    void isValidCardNumber_shouldReturnFalseForNull() {
        assertFalse(ValidationUtil.isValidCardNumber(null));
    }

    @Test
    void isValidCardNumber_shouldHandleSpacesAndDashes() {
        assertTrue(ValidationUtil.isValidCardNumber("4111-1111-1111-1111"));
        assertTrue(ValidationUtil.isValidCardNumber("4111 1111 1111 1111"));
    }

    @Test
    void isValidFilterName_shouldReturnTrueForValidName() {
        assertTrue(ValidationUtil.isValidFilterName("stolen-cards"));
        assertTrue(ValidationUtil.isValidFilterName("filter_name_123"));
        assertTrue(ValidationUtil.isValidFilterName("FilterName"));
    }

    @Test
    void isValidFilterName_shouldReturnFalseForInvalidName() {
        assertFalse(ValidationUtil.isValidFilterName("invalid name"));
        assertFalse(ValidationUtil.isValidFilterName("name.with.dots"));
        assertFalse(ValidationUtil.isValidFilterName(null));
        assertFalse(ValidationUtil.isValidFilterName(""));
    }

    @Test
    void isValidAmount_shouldReturnTrueForPositiveAmount() {
        assertTrue(ValidationUtil.isValidAmount(new BigDecimal("100.00")));
        assertTrue(ValidationUtil.isValidAmount(new BigDecimal("0.01")));
    }

    @Test
    void isValidAmount_shouldReturnFalseForZeroOrNegative() {
        assertFalse(ValidationUtil.isValidAmount(BigDecimal.ZERO));
        assertFalse(ValidationUtil.isValidAmount(new BigDecimal("-100.00")));
        assertFalse(ValidationUtil.isValidAmount(null));
    }

    @Test
    void sanitizeCardNumber_shouldRemoveSpacesAndDashes() {
        assertEquals("4111111111111111", 
                ValidationUtil.sanitizeCardNumber("4111-1111-1111-1111"));
        assertEquals("4111111111111111", 
                ValidationUtil.sanitizeCardNumber("4111 1111 1111 1111"));
    }
}
