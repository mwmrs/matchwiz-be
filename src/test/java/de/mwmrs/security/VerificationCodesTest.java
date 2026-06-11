package de.mwmrs.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VerificationCodesTest {

    @Test
    void generateProducesCodesFromAlphabet() {
        for (int i = 0; i < 100; i++) {
            String code = VerificationCodes.generate();
            assertEquals(VerificationCodes.LENGTH, code.length());
            for (char c : code.toCharArray()) {
                assertTrue(VerificationCodes.ALPHABET.indexOf(c) >= 0, "unexpected char: " + c);
            }
        }
    }

    @Test
    void normalizeToleratesUserInputVariants() {
        assertEquals("ABCD2345", VerificationCodes.normalize(" abcd-2345 "));
        assertEquals("ABCD2345", VerificationCodes.normalize("AB CD 23 45"));
        assertEquals("", VerificationCodes.normalize(null));
    }

    @Test
    void sha256IsDeterministicHex() {
        String a = VerificationCodes.sha256("ABCD2345");
        String b = VerificationCodes.sha256("ABCD2345");
        assertEquals(a, b);
        assertEquals(64, a.length());
        assertTrue(a.matches("[0-9a-f]{64}"));
        assertNotEquals(a, VerificationCodes.sha256("ABCD2346"));
    }
}
