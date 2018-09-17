package io.raspberrywallet.manager.cryptography.crypto.decryption;

import io.raspberrywallet.manager.cryptography.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.wrappers.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.wrappers.crypto.RSAEncryptedObject;
import io.raspberrywallet.manager.cryptography.wrappers.data.Password;

import java.io.Serializable;
import java.security.PrivateKey;

public interface DecryptionObject {
    
    /**
     * This method is used for decryption of an object, that is wrapped in AESEncryptedObject class, which represents
     * serialized and encrypted data of an original object, with AES parameters that were used for encryption.
     * @param object Wrapped AES encrypted object, which is going to be decrypted.
     * @param password Password that will be used for creating PBBKeySpec for AES decryption.
     * @param <E> A type of an object, that is stored inside AESEncryptedObject wrapper class.
     * @return Original, decrypted and deserialized object, based on given data from AESEncryptedObject.
     * @throws DecryptionException If there is any error in decryption, then it is caught and thrown as
     *                             DecryptionException, with it's original or custom message.
     */
    <E extends Serializable> E decryptObject(AESEncryptedObject<E> object, Password password) throws DecryptionException;
    
    /**
     * This method is used for decryption of an object, that is wrapped in RSAEncryptedObject class, which represents
     * serialized and encrypted data of an original object, with RSA parameters that were used for encryption.
     * @param object Wrapped RSA encrypted object, which is going to be decrypted.
     * @param privateKey Private RSA key needed for decryption.
     * @param <E> The type of an object, that is stored inside RSAEncryptedObject wrapper class.
     * @return Original, decrypted and deserialized object, based on given data from RSAEncryptedObject.
     * @throws DecryptionException If there is any error in decryption, then it is caught and thrown as
     *                             DecryptionException, with it's original or custom message.
     */
    <E extends Serializable> E decryptObject(RSAEncryptedObject<E> object, PrivateKey privateKey) throws DecryptionException;
    
}
