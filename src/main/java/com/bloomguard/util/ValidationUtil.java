package com.bloomguard.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern FILTER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private ValidationUtil() {}

    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        return CARD_NUMBER_PATTERN.matcher(cleaned).matches() && luhnCheck(cleaned);
    }

    public static boolean isValidFilterName(String filterName) {
        if (filterName == null || filterName.isEmpty() || filterName.length() > 100) {
            return false;
        }
        return FILTER_NAME_PATTERN.matcher(filterName).matches();
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean luhnCheck(String number) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }

    public static String sanitizeCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        return cardNumber.replaceAll("[\\s-]", "");
    }
}
