package io.raspberrywallet.manager.cryptography.crypto.algorithms;

import java.io.Serializable;

public abstract class AlgorithmParams implements Serializable {
    
    String algorithmName;
    String algorithmFullName;
    
    int keySize;
    
    public String getAlgorithmName() {
        return algorithmName;
    }
    
    public String getFullAlgorithmName() {
        return algorithmFullName;
    }
    
    public int getKeySize() {
        return keySize;
    }
    
}
