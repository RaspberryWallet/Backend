import io.raspberrywallet.manager.cryptography.sharedsecret.blakley.Blakley;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class BlakleySharedSecretTest {
    final static int n = 3; //number of generate keys
    final static int t = 2; //number of keys for solve the secret (t <= n)
    final static int bits = 512; //number of bits of keys

    @Test
    public void testShamir() {

        BigInteger keys[][] = new BigInteger[n][];
        BigInteger keys2[][] = new BigInteger[t][];
        BigInteger pass[] = new BigInteger[t];

        String secret = "Secret Sharing";

        //Divide secret in parts (coordinates).
        pass = Blakley.divide(t, secret.getBytes());

        //Generate n keys
        for (int i = 0; i < n; i++)
            keys[i] = Blakley.createdKey(pass, bits);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < keys[i].length; j++) {
                System.out.println("Key[" + i + "] = " + keys[i][j].toString());
            }
        }

        //Select 2 keys from 3

        keys2[0] = keys[0];
        keys2[1] = keys[1];

        //solve
        byte[] b = Blakley.solutionKey(keys2);

        //Convert to String
        String text = new String(b);
        System.out.println("From first and second secret: " + text);


        keys2[0] = keys[0];
        keys2[1] = keys[2];
        //solve
        b = Blakley.solutionKey(keys2);

        //Convert to String
        text = new String(b);
        System.out.println("From first and second third: " + text);


        keys2[1] = keys[1];
        keys2[1] = keys[2];
        //solve
        b = Blakley.solutionKey(keys2);

        //Convert to String
        text = new String(b);
        System.out.println("From second and third secret: " + text);
    }

}
