package io.raspberrywallet.manager.cryptography.crypto.algorithms;

class RSAParameters extends AlgorithmFactory {
    
    RSAParameters() {
        algorithmName = "RSA";
        algorithmFullName = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
        keySize = 2048;
    }

}
