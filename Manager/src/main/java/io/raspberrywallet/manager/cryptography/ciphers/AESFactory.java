package io.raspberrywallet.manager.cryptography.ciphers;

public class AESFactory extends AlgorithmFactory {
    
    private final static String HASH_ALGORITHM_NAME = "PBKDF2WithHmacSHA1";
    
    private final static int KEY_HASH_ITERATIONS_AMOUNT = 65536;
    
    public AESFactory() {
        algorithmName = "AES";
        algorithmFullName = "AES/CBC/PKCS5Padding";
        keySize = 256;
    }

    public String getHashAlgorithmName() {
        return HASH_ALGORITHM_NAME;
    }

    public int getKeyHashIterationsAmount() {
        return KEY_HASH_ITERATIONS_AMOUNT;
    }
    
    public int getKeySaltSize() {
        return 16;
    }
    
    public int getIvSaltSize() {
        return 16;
    }
    
}
