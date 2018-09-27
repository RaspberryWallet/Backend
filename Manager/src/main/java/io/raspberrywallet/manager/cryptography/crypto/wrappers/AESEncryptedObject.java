package io.raspberrywallet.manager.cryptography.crypto.wrappers;


import io.raspberrywallet.manager.cryptography.crypto.ciphers.AESCipherFactory;

import java.io.Serializable;

public class AESEncryptedObject<E extends Serializable> extends EncryptedObject<E> implements Serializable {
    
    
    public AESEncryptedObject(byte[] serializedObject, AESCipherFactory cipherFactory) {
        super(serializedObject, cipherFactory, true);
    }
    
    public AESEncryptedObject(byte[] serializedObject, AESCipherFactory cipherFactory, boolean isEncrypted) {
        super(serializedObject, cipherFactory, isEncrypted);
    }
    
    @Override
    public AESCipherFactory getCipherFactory() {
        return (AESCipherFactory) cipherFactory;
    }
    
}
