package io.raspberrywallet.manager.cryptography.wrappers.crypto;


import io.raspberrywallet.manager.cryptography.ciphers.RSACipherFactory;

import javax.crypto.IllegalBlockSizeException;
import java.io.Serializable;

public class RSAEncryptedObject<E extends Serializable> extends EncryptedObject<E> implements Serializable {
    
    public RSAEncryptedObject(byte[] serializedObject, RSACipherFactory cipherFactory) throws IllegalBlockSizeException {
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

