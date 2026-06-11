package de.mwmrs.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Generation and hashing of short, user-typeable verification codes.
 * The alphabet omits ambiguous characters (0/O, 1/I); 8 characters out of 32
 * give ~40 bits of entropy, which combined with a short expiry and single use
 * makes online brute force impractical.
 */
public final class VerificationCodes {

    public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final int LENGTH = 8;

    private static final SecureRandom RANDOM = new SecureRandom();

    private VerificationCodes() {
    }

    public static String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /** Hex-encoded SHA-256; codes are stored hashed at rest. */
    public static String sha256(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Tolerates lowercase, surrounding whitespace, and dashes/spaces typed between groups. */
    public static String normalize(String input) {
        return input == null ? "" : input.replaceAll("[\\s-]", "").toUpperCase();
    }
}
