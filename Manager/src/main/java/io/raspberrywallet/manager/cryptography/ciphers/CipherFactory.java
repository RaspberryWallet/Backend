package io.raspberrywallet.manager.cryptography.ciphers;

import java.io.Serializable;

public abstract class CipherFactory implements Serializable {
    
    int keySize;
    
    String algorithmName;
    String algorithmFullName;
    
    public String getAlgorithmName() {
        return algorithmName;
    }
    
    public String getAlgorithmFullName() {
        return algorithmFullName;
    }
    
    public int getKeySize() {
        return keySize;
    }
    
}
