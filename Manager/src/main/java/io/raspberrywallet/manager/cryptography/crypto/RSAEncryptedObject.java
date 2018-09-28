package io.raspberrywallet.manager.cryptography.crypto;


import io.raspberrywallet.manager.cryptography.crypto.algorithms.RSACipherParams;

import java.io.Serializable;

public class RSAEncryptedObject<E extends Serializable> extends EncryptedObject<E> implements Serializable {
    
    public RSAEncryptedObject(byte[] serializedObject, RSACipherParams cipherFactory) {
        super(serializedObject, cipherFactory, true);
    }
    
    public RSAEncryptedObject(byte[] serializedObject, RSACipherParams cipherFactory, boolean isEncrypted) {
        super(serializedObject, cipherFactory, isEncrypted);
    }
    
    @Override
    public RSACipherParams getCipherParams() {
        return (RSACipherParams) cipherParams;
    }
}

