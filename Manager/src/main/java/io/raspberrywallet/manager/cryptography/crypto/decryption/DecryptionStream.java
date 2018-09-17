package io.raspberrywallet.manager.cryptography.crypto.decryption;

import io.raspberrywallet.manager.cryptography.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.wrappers.data.Password;

import java.security.PrivateKey;

public interface DecryptionStream {
    
    /**
     * This method is used for decryption of encrypted data in given input stream. The encryption algorithm must be AES.
     * The input stream must contain AESFactory serialized header, which was used to encryption.
     * @param password Password that will be used for creating PBBKeySpec for AES decryption.
     * @throws DecryptionException If there is any error in decryption, then it is caught and thrown as
     *                             DecryptionException, with it's original or custom message.
     */
    void decryptStream(Password password) throws DecryptionException;
    
    /**
     * This method is used for decryption of encrypted data in given input stream. The encryption algorithm must be RSA.
     * The input stream must contain RSAFactory serialized header, which was used to encryption.
     * @param privateKey Private RSA key needed for decryption.
     * @throws DecryptionException If there is any error in decryption, then it is caught and thrown as
     *                             DecryptionException, with it's original or custom message.
     */
    void decryptStream(PrivateKey privateKey) throws DecryptionException;
    
}
