package com.users.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CpfValidatorTest {
    @Test
    void validatesDigitsAndNormalizesPunctuation() {
        assertEquals("52998224725", CpfValidator.normalize("529.982.247-25"));
        for (String value :
                new String[] {"11111111111", "52998224726", "abc", "123", "52998224725x"})
            assertThrows(IllegalArgumentException.class, () -> CpfValidator.normalize(value));
        assertThrows(IllegalArgumentException.class, () -> CpfValidator.normalize(null));
    }
}
