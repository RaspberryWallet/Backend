package io.raspberrywallet.manager.cryptography.sharedsecret.shamir;


import org.bitcoinj.core.Base58;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * <p> Contains a Shamir's key <br>
 * f = poly(x) mod p </p>
 */
public class ShamirKey {
    private BigInteger p;
    private BigInteger f;
    private BigInteger x;

    ShamirKey() {

    }

    public ShamirKey(BigInteger p, BigInteger f, BigInteger x) {
        this.p = p;
        this.f = f;
        this.x = x;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public void setF(BigInteger f) {
        this.f = f;
    }

    public void setX(BigInteger x) {
        this.x = x;
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getF() {
        return f;
    }

    public BigInteger getX() {
        return x;
    }

    public byte[] toByteArray() {
        String baseP = Base58.encode(p.toByteArray());
        String baseF = Base58.encode(f.toByteArray());
        String baseX = Base58.encode(x.toByteArray());

        String total = baseP + ":" + baseF + ":" + baseX;
        return total.getBytes();
    }

    /**
     * @param bytes three parameters p,f and x encoded in UTF-8 and formatted like Base58(p):Base58(x):Base58(f)
     * @return ShamirKey of p, f and x
     */
    @NotNull
    public static ShamirKey fromByteArray(@NotNull byte[] bytes) {
        if (bytes.length <= 0) throw new IllegalArgumentException("bytes can not be empty");
        String total = new String(bytes);

        String[] parts = total.split(":");
        if (parts.length != 3)
            throw new IllegalArgumentException("could not find all 3 formula parts p, x and f separated by \':\' in " + total);

        BigInteger p = Base58.decodeToBigInteger(parts[0]);
        BigInteger f = Base58.decodeToBigInteger(parts[1]);
        BigInteger x = Base58.decodeToBigInteger(parts[2]);
        return new ShamirKey(p, f, x);
    }

}