package io.raspberrywallet.manager.cryptography.crypto.ciphers;

public class RSAFactory extends AlgorithmFactory {
    
    public RSAFactory() {
        algorithmName = "RSA";
        algorithmFullName = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
        keySize = 2048;
    }

}
