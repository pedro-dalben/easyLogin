package com.easylogin.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Password hashing and verification using BCrypt.
 * Pure Java — no native dependencies.
 */
public final class PasswordService {

    private static final int BCRYPT_COST = 12;

    private PasswordService() {
    }

    /**
     * Hash a plaintext password with BCrypt.
     *
     * @param password the plaintext password
     * @return the BCrypt hash string (includes salt and cost)
     */
    public static String hash(String password) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray());
    }

    /**
     * Verify a plaintext password against a BCrypt hash.
     *
     * @param password the plaintext password to verify
     * @param hash     the stored BCrypt hash
     * @return true if the password matches
     */
    public static boolean verify(String password, String hash) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        return result.verified;
    }
}
