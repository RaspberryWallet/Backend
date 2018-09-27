package io.raspberrywallet.manager.cryptography.crypto;

class RSAFactory extends AlgorithmFactory {
    
    RSAFactory() {
        algorithmName = "RSA";
        algorithmFullName = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
        keySize = 2048;
    }

}
