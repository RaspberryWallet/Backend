package io.raspberrywallet.manager.cryptography.crypto;

import java.io.Serializable;

abstract class CipherFactory implements Serializable {
    
    int keySize;
    
    String algorithmName;
    String algorithmFullName;
    
    String getAlgorithmName() {
        return algorithmName;
    }
    
    String getAlgorithmFullName() {
        return algorithmFullName;
    }
    
    int getKeySize() {
        return keySize;
    }
    
}
