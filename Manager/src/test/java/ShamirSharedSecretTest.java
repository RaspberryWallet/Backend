import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirException;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirKey;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirSharedSecret;
import org.junit.jupiter.api.Test;


import java.math.BigInteger;

public class ShamirSharedSecretTest {
    final static int n = 3; //number of generate shares
    final static int t = 2; //number of shares for solve the secret (t <= n)

    @Test
    public void testShamir() {

        String secret = "KotlinIsTheBest";
        System.out.println("Secret = " + secret);
        int numBits = secret.length() * 8; //We need bits not bytes

        //Create key
        ShamirKey[] sk;
        BigInteger[] s;
        try {
            s = ShamirSharedSecret.generateParameters(t, numBits, secret.getBytes());
            sk = ShamirSharedSecret.generateKeys(n, t, numBits, s);
        } catch (ShamirException sE) {
            System.out.println("Error while generate shamir keys");
            return;
        }

        ShamirKey[] sk2 = new ShamirKey[t];

        //Act
        sk2[0] = sk[0];
        sk2[1] = sk[1];
        byte[] des = ShamirSharedSecret.calculateLagrange(sk2);
        System.out.println("First and second keys used for restoring  Secret = " + new String(des));
        assert new String(des).equals(secret);

        sk2[0] = sk[0];
        sk2[1] = sk[2];
        des = ShamirSharedSecret.calculateLagrange(sk2);
        System.out.println("First and third keys used for restoring Secret = " + new String(des));
        assert new String(des).equals(secret);

        sk2[0] = sk[1];
        sk2[1] = sk[2];
        des = ShamirSharedSecret.calculateLagrange(sk2);
        System.out.println("Second and third keys used for restoring Secret = " + new String(des));
        assert new String(des).equals(secret);

    }
}