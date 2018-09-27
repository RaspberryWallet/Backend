package io.raspberrywallet.manager.cryptography.crypto;

import io.raspberrywallet.manager.cryptography.crypto.algorithms.AESCipherFactory;
import io.raspberrywallet.manager.cryptography.crypto.algorithms.RSACipherFactory;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.common.Password;
import org.apache.commons.lang.SerializationUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class CryptoObject {
    
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
    public <E extends Serializable> AESEncryptedObject<E> encrypt(E object, Password password) throws EncryptionException {
        AESCipherFactory aesCipherFactory = new AESCipherFactory();
        
        try {
            Cipher cipher = aesCipherFactory.getCipher(password, Cipher.ENCRYPT_MODE);
    
            byte[] encryptedObject = encrypt(object, cipher);
            return new AESEncryptedObject<>(encryptedObject, aesCipherFactory);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException exception) {
            throw new EncryptionException(exception);
        }
    }
    
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
    public <E extends Serializable> RSAEncryptedObject<E> encrypt(E object, PublicKey publicKey) throws EncryptionException {
        RSACipherFactory rsaCipherFactory = new RSACipherFactory();
        try {
            Cipher cipher = rsaCipherFactory.getEncryptCipher(publicKey);
            
            byte[] encryptedObject = encrypt(object, cipher);
            return new RSAEncryptedObject<>(encryptedObject, rsaCipherFactory);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException exception) {
            throw new EncryptionException(exception);
        }
    }
    
    private byte[] encrypt(Serializable object, Cipher cipher) throws BadPaddingException, IllegalBlockSizeException {
        byte[] serializedObject = SerializationUtils.serialize(object);
        return cipher.doFinal(serializedObject);
    }
    
    // decryption
    
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
    public <E extends Serializable> E decrypt(AESEncryptedObject<E> object, Password password) throws DecryptionException {
        if (!object.isEncrypted())
            throw new DecryptionException("Given object is not encrypted.");
        
        AESCipherFactory cipherFactory = object.getCipherFactory();
        try {
            Cipher cipher = cipherFactory.getCipher(password, Cipher.DECRYPT_MODE);
            
            return decrypt(object, cipher);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException exception) {
            throw new DecryptionException(exception);
        }
    }
    
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
    public <E extends Serializable> E decrypt(RSAEncryptedObject<E> object, PrivateKey privateKey) throws DecryptionException {
        if(!object.isEncrypted())
            throw new DecryptionException("Given object is not encrypted.");
        
        RSACipherFactory cipherFactory = object.getCipherFactory();
        try {
            Cipher cipher = cipherFactory.getDecryptCipher(privateKey);
    
            return decrypt(object, cipher);
        } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException exception) {
            throw new DecryptionException(exception);
        }
    }
    
    private <E extends Serializable> E decrypt(EncryptedObject<E> object, Cipher cipher) throws BadPaddingException, IllegalBlockSizeException {
        byte[] decryptedObject = cipher.doFinal(object.getSerializedObject());
        return object.getOriginalObject(decryptedObject);
    }
    
}
