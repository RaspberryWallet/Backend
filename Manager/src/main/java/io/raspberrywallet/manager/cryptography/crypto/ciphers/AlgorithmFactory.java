package io.raspberrywallet.manager.cryptography.crypto.ciphers;

import java.io.Serializable;

public abstract class AlgorithmFactory implements Serializable {
    
    String algorithmName;
    String algorithmFullName;
    
    int keySize;
    
    String getAlgorithmName() {
        return algorithmName;
    }
    
    String getFullAlgorithmName() {
        return algorithmFullName;
    }
    
    int getKeySize() {
        return keySize;
    }
    
}
