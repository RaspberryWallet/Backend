package io.raspberrywallet.manager.cryptography.crypto.encryption;


import io.raspberrywallet.manager.cryptography.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.wrappers.data.Password;

import java.security.PublicKey;

public interface EncryptionStream {
    
    /**
     * This method is using AES algorithm to encrypt given input stream and write it to given output stream.
     * Output stream contain serialized header with metadata, that can be used for decryption.
     * @param password Password that will be used with PBEKeySpec for AES encryption.
     * @throws EncryptionException If there is any error with encryption, then it's caught and thrown as
     *                             EncryptionException, with original or custom error message.
     */
    void encryptStream(Password password) throws EncryptionException;
    
    /**
     * This method is using AES algorithm to encrypt given input stream and write it to given output stream.
     * Output stream contains serialized header with metadata, that can be used for decryption.
     * @param publicKey Public key used for encryption.
     */
    void encryptStream(PublicKey publicKey) throws EncryptionException;
    
}
