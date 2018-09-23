package io.raspberrywallet.manager.cryptography.sharedsecret.shamir;


import org.junit.jupiter.api.Test;

import java.math.BigInteger;

class ShamirTest {
    private final int totalShares = 3; //number of generate shares
    private final int requiredShares = 2; //number of shares for solve the secret (requiredShares <= totalShares)

    @Test
    void testShamir() throws ShamirException {
        String secret = "KotlinIsTheBest";
        System.out.println("Secret = " + secret);
        int numBits = secret.length() * 8; //We need bits not bytes

        //Create key
        BigInteger[] polynomialParams = Shamir.generateParams(requiredShares, numBits, secret.getBytes());
        ShamirKey[] allKeys = Shamir.generateKeys(totalShares, requiredShares, numBits, polynomialParams);

        //Act
        String secretRestored = restoreSecretWith(allKeys[0], allKeys[1]);
        System.out.println("1st and 2nd keys used for restoring  Secret = " + secretRestored);
        assert secretRestored.equals(secret);

        secretRestored = restoreSecretWith(allKeys[0], allKeys[2]);
        System.out.println("1st and 3rd keys used for restoring  Secret = " + secretRestored);
        assert secretRestored.equals(secret);

        secretRestored = restoreSecretWith(allKeys[1], allKeys[2]);
        System.out.println("2nd and 3rd keys used for restoring  Secret = " + secretRestored);
        assert secretRestored.equals(secret);
    }

    private String restoreSecretWith(ShamirKey... keys) {
        byte[] des = Shamir.calculateLagrange(keys);
        return new String(des);
    }

    @Test
    void testShamirSpeed() throws ShamirException {
        String secret = "KotlinIsTheBest";
        System.out.println("Secret = " + secret);
        int numBits = secret.length() * 8; //We need bits not bytes

        //Create key
        BigInteger[] polynomialParams = Shamir.generateParams(requiredShares, numBits, secret.getBytes());
        ShamirKey[] allKeys = Shamir.generateKeys(totalShares, requiredShares, numBits, polynomialParams);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100_000; i++) {
            restoreSecretWith(allKeys[0], allKeys[1]);
        }
        long totalTime = System.currentTimeMillis() - start;
        System.out.format("Total time [s] = %.3f", totalTime / 1000d);
    }
}