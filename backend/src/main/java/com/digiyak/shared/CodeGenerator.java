package com.digiyak.shared;

import java.security.SecureRandom;

/**
 * Cryptographically strong generator for human-friendly Digiyak codes & handoff PINs.
 */
public final class CodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private CodeGenerator() {
    }

    /** 4-digit numeric PIN for Pickup Code or Delivery Handoff PIN (0000-9999). */
    public static String fourDigitPin() {
        return String.format("%04d", RANDOM.nextInt(10_000));
    }

    /** Human-readable delivery tracking code, e.g. DYK-48291. */
    public static String trackingCode() {
        return "DYK-" + String.format("%05d", RANDOM.nextInt(100_000));
    }

    /** URL-safe short code for 1-tap WhatsApp payment links, e.g. 8 chars without ambiguous O/0/I/1. */
    public static String shortPayLinkCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)]);
        }
        return sb.toString();
    }
}
