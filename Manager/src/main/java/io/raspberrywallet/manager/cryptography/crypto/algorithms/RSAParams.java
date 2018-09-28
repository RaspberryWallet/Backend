package io.raspberrywallet.manager.cryptography.crypto.algorithms;

class RSAParams extends AlgorithmParams {
    
    RSAParams() {
        algorithmName = "RSA";
        algorithmFullName = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
        keySize = 2048;
    }

}
