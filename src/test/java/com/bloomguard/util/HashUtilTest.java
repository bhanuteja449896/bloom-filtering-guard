package com.bloomguard.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {

    @Test
    void sha256_shouldReturnConsistentHash() {
        String input = "test-input";
        
        String hash1 = HashUtil.sha256(input);
        String hash2 = HashUtil.sha256(input);

        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
    }

    @Test
    void sha256_shouldReturnDifferentHashForDifferentInputs() {
        String hash1 = HashUtil.sha256("input1");
        String hash2 = HashUtil.sha256("input2");

        assertNotEquals(hash1, hash2);
    }

    @Test
    void sha256_shouldThrowExceptionForNullInput() {
        assertThrows(IllegalArgumentException.class, () -> HashUtil.sha256(null));
    }

    @Test
    void sha256WithSalt_shouldReturnDifferentHashThanWithoutSalt() {
        String input = "test-input";
        String salt = "salt123";

        String hashWithoutSalt = HashUtil.sha256(input);
        String hashWithSalt = HashUtil.sha256(input, salt);

        assertNotEquals(hashWithoutSalt, hashWithSalt);
    }
}
