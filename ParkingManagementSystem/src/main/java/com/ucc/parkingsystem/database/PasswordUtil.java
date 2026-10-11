package com.ucc.parkingsystem.database;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PasswordUtil {

    private static final int ITERATIONS = 65536;   // how many times PBKDF2 repeats itself
    private static final int KEY_LENGTH = 256;      // size of the resulting hash, in bits
    private static final int SALT_LENGTH = 16;      // size of the random salt, in bytes

    // Creates a new hash for a plain password, with a fresh random salt.
    // Stored as "salt:hash", both Base64-encoded text, so it fits in one TEXT column.
    public static String hash(String plainPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] hashBytes = pbkdf2(plainPassword.toCharArray(), salt);

        return Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hashBytes);
    }

    // Checks a plain password against a stored "salt:hash" value.
    public static boolean verify(String plainPassword, String stored) {
        String[] parts = stored.split(":");
        if (parts.length != 2) {
            return false;   // not a recognized hash, so it can never match
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
        byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt);

        // MessageDigest.isEqual compares in constant time, so the comparison
        // itself can't leak information about how many characters matched.
        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Could not hash password", e);
        }
    }
}