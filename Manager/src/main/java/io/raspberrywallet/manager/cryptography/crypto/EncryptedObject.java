package io.raspberrywallet.manager.cryptography.crypto;



import io.raspberrywallet.manager.cryptography.crypto.algorithms.CipherParams;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;

public abstract class EncryptedObject<E extends Serializable> implements Serializable {
    
    private static final long serializationUUID = 63465294432588745L;
    
    private byte[] serializedObject;
    CipherParams cipherParams;
    private boolean isEncrypted;
    
    public EncryptedObject(byte[] serializedObject, CipherParams cipherParams, boolean isEncrypted) {
        this.serializedObject = serializedObject;
        this.cipherParams = cipherParams;
        this.isEncrypted = isEncrypted;
    }
    
    public E getOriginalObject(byte[] data) {
        return (E)SerializationUtils.deserialize(data);
    }
    
    public byte[] getSerializedObject() {
        return serializedObject;
    }
    
    public boolean isEncrypted() {
        return isEncrypted;
    }
    
    public abstract CipherParams getCipherParams();
    
    
}
