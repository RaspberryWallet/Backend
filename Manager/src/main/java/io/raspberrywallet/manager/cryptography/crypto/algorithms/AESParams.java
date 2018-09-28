package io.raspberrywallet.manager.cryptography.crypto.algorithms;

class AESParams extends AlgorithmParams {
    
    private final static String HASH_ALGORITHM_NAME = "PBKDF2WithHmacSHA1";
    
    private final static int KEY_HASH_ITERATIONS_AMOUNT = 65536;
    
    AESParams() {
        algorithmName = "AES";
        algorithmFullName = "AES/CBC/PKCS5Padding";
        keySize = 256;
    }
    
    String getHashAlgorithmName() {
        return HASH_ALGORITHM_NAME;
    }
    
    int getKeyHashIterationsAmount() {
        return KEY_HASH_ITERATIONS_AMOUNT;
    }
    
    int getKeySaltSize() {
        return 16;
    }
    
    int getIvSaltSize() {
        return 16;
    }
    
}
