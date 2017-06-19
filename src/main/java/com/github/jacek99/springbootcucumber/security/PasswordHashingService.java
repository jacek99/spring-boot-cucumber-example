package com.github.jacek99.springbootcucumber.security;

import com.google.common.base.Charsets;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import lombok.Value;
import org.springframework.stereotype.Component;

/**
 * Service for secure hashing of password
 */
@Component
public class PasswordHashingService {

    private static final int MIN_REPETITIONS = 1000;

    /**
     * Hashes a password, with a unique salt and random
     * number of iterations (i.e. nested hashes, to prevent rainbow
     * table attacks)
     */
    public HashResult hashPassword(String password) {

        try {
            char[] chars = password.toCharArray();
            byte[] salt = getSalt().getBytes(Charsets.UTF_8);
            int repetitions = getRepetitions();

            PBEKeySpec spec = new PBEKeySpec(chars, salt, repetitions, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = skf.generateSecret(spec).getEncoded();

            return new HashResult(toHex(hash), toHex(salt), repetitions);


        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Validates if a password is valid
     */
    public boolean isHashValid(String password, String expectedHash,
                               String salt, int repetitions) {

        try {

            byte[] rawHash = fromHex(expectedHash);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(),
                    fromHex(salt),
                    repetitions,
                    rawHash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            return Arrays.equals(testHash,rawHash);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }

    private int getRepetitions() {
        return MIN_REPETITIONS + ThreadLocalRandom.current().nextInt(1000);
    }

    private String getSalt()  {
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return Arrays.toString(salt);
    }


    private String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

    private byte[] fromHex(String hex) throws NoSuchAlgorithmException
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }


    @Value
    public static class HashResult {
        private String passwordHash;
        private String salt;
        // how many times the hash was performed, for further obstruction
        // and to prevent rainbow table attacks
        private int repetitions;
    }

}
