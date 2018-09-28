package io.raspberrywallet.manager.cryptography.crypto;


import io.raspberrywallet.manager.cryptography.crypto.algorithms.AESCipherParams;

import java.io.Serializable;

public class AESEncryptedObject<E extends Serializable> extends EncryptedObject<E> implements Serializable {
    
    
    public AESEncryptedObject(byte[] serializedObject, AESCipherParams cipherFactory) {
        super(serializedObject, cipherFactory, true);
    }
    
    public AESEncryptedObject(byte[] serializedObject, AESCipherParams cipherFactory, boolean isEncrypted) {
        super(serializedObject, cipherFactory, isEncrypted);
    }
    
    @Override
    public AESCipherParams getCipherParams() {
        return (AESCipherParams) cipherParams;
    }
    
}
