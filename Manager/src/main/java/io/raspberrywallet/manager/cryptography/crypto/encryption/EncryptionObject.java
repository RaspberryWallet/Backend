package io.raspberrywallet.manager.cryptography.crypto.encryption;

import io.raspberrywallet.manager.cryptography.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.wrappers.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.wrappers.crypto.RSAEncryptedObject;
import io.raspberrywallet.manager.cryptography.wrappers.data.Password;

import java.io.Serializable;
import java.security.PublicKey;


/**
 * Classes implementing this interface
 */

public interface EncryptionObject {
    
    /**
     * This method is using AES algorithm to encrypt given object and wrap it with AESEncryptedObject class,
     * which represents encrypted, serialized object data and it's AES parameters needed for decryption.
     * @param object Any object implementing Serializable interface.
     * @param password Password that will be used with PBEKeySpec for AES encryption.
     * @param <E> The type of an object, that is going to be encrypted.
     * @return Wrapped in AESEncryptedObject serialized bytes of original object with AES parameters, needed for decryption.
     * @throws EncryptionException If there is any error with encryption, then it's caught and thrown as
     *                             EncryptionException, with original or custom error message.
     */
    <E extends Serializable> AESEncryptedObject<E> encryptObject(E object, Password password) throws EncryptionException;
    
    /**
     * This method is using RSA algorithm to encrypt given object and wrap it with RSAEncryptedObject class,
     * which is representing encrypted, serialized object data and it's RSA parameters needed for decryption.
     * @param object Any object implementing Serializable interface.
     * @param publicKey Public key used for encryption.
     * @param <E> The type of an object, that is going to be encrypted.
     * @return Wrapped in RSAEncryptedObject serialized bytes of original object with RSA parameters, needed for
     *         decryption. It's completely safe to save it without any more encryption, because it doesn't contain
     *         any confidential data.
     * @throws EncryptionException If there is any error with encryption, then it's caught and thrown as
     *                             EncryptionException, with original or custom error message.
     */
    <E extends Serializable> RSAEncryptedObject<E> encryptObject(E object, PublicKey publicKey) throws EncryptionException;
    
}
