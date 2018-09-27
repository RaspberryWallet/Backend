package io.raspberrywallet.manager.cryptography.sharedsecret.blakley;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static io.raspberrywallet.manager.Utils.println;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BlakleyTest {
    private final int totalShares = 3; //number of generate keys
    private final int requiredShares = 2; //number of keys for solve the secret (requiredShares <= totalShares)
    private final int bits = 512; //number of bits of keys

    @Test
    void testBlakley() {
        BigInteger allKeys[][] = new BigInteger[totalShares][];
        String secret = "Secret Sharing";

        //Divide secret in parts (coordinates).
        BigInteger[] pass = Blakley.divide(requiredShares, secret.getBytes());

        //Generate totalShares keys
        for (int i = 0; i < totalShares; i++)
            allKeys[i] = Blakley.createdKey(pass, bits);

        for (int i = 0; i < totalShares; i++) {
            for (int j = 0; j < allKeys[i].length; j++) {
                System.out.println("Key[" + i + "] = " + allKeys[i][j].toString());
            }
        }

        //Select 2 keys from 3
        String secretRestored = restoreSecretWith(allKeys[0], allKeys[1]);
        assertEquals(secretRestored, secret);
        println("From first and second secret: " + secretRestored);

        secretRestored = restoreSecretWith(allKeys[0], allKeys[2]);
        assertEquals(secretRestored, secret);
        println("From first and second third: " + secretRestored);

        //Convert to String
        secretRestored = restoreSecretWith(allKeys[1], allKeys[2]);
        assertEquals(secretRestored, secret);
        println("From second and third secret: " + secretRestored);
    }

    private String restoreSecretWith(BigInteger[]... keys) {
        byte[] des = Blakley.solutionKey(keys);
        return new String(des);
    }

    @Test
    void testBlakleySpeed() {
        BigInteger allKeys[][] = new BigInteger[totalShares][];
        String secret = "Secret Sharing";

        //Divide secret in parts (coordinates).
        BigInteger[] pass = Blakley.divide(requiredShares, secret.getBytes());

        //Generate totalShares keys
        for (int i = 0; i < totalShares; i++)
            allKeys[i] = Blakley.createdKey(pass, bits);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100_000; i++) {
            restoreSecretWith(allKeys[0], allKeys[1]);
        }
        long totalTime = System.currentTimeMillis() - start;
        System.out.format("Total time [s] = %.3f", totalTime / 1000d);
    }

}
