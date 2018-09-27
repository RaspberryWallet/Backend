package io.raspberrywallet.manager.cryptography.crypto.wrappers;


import io.raspberrywallet.manager.cryptography.crypto.RSACipherFactory;

import java.io.Serializable;

public class RSAEncryptedObject<E extends Serializable> extends EncryptedObject<E> implements Serializable {
    
    public RSAEncryptedObject(byte[] serializedObject, RSACipherFactory cipherFactory) {
        super(serializedObject, cipherFactory, true);
    }
    
    public RSAEncryptedObject(byte[] serializedObject, RSACipherFactory cipherFactory, boolean isEncrypted) {
        super(serializedObject, cipherFactory, isEncrypted);
    }
    
    @Override
    public RSACipherFactory getCipherFactory() {
        return (RSACipherFactory)cipherFactory;
    }
}

