package com.bloomguard.util;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public final class FingerprintUtil {

    private FingerprintUtil() {}

    public static String createTransactionFingerprint(String accountId, BigDecimal amount, 
                                                      String currency, String recipient) {
        StringBuilder sb = new StringBuilder();
        sb.append(normalize(accountId)).append("|");
        sb.append(amount.stripTrailingZeros().toPlainString()).append("|");
        sb.append(normalize(currency)).append("|");
        sb.append(normalize(recipient));
        return sb.toString();
    }

    public static String createInvoiceFingerprint(String invoiceNumber, String vendorId, BigDecimal amount) {
        StringBuilder sb = new StringBuilder();
        sb.append(normalize(invoiceNumber)).append("|");
        sb.append(normalize(vendorId)).append("|");
        sb.append(amount.stripTrailingZeros().toPlainString());
        return sb.toString();
    }

    public static String hashFingerprint(String fingerprint) {
        return HashUtil.sha256(fingerprint);
    }

    private static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().toLowerCase();
    }
}
