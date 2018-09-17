package io.raspberrywallet.manager.cryptography.crypto;

import io.raspberrywallet.manager.cryptography.ciphers.AESCipherFactory;
import io.raspberrywallet.manager.cryptography.ciphers.AESFactory;
import io.raspberrywallet.manager.cryptography.ciphers.RSACipherFactory;
import io.raspberrywallet.manager.cryptography.ciphers.RSAFactory;
import io.raspberrywallet.manager.cryptography.crypto.decryption.DecryptionObject;
import io.raspberrywallet.manager.cryptography.crypto.encryption.EncryptionObject;
import io.raspberrywallet.manager.cryptography.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.wrappers.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.wrappers.crypto.RSAEncryptedObject;
import io.raspberrywallet.manager.cryptography.wrappers.data.Password;
import org.apache.commons.lang.SerializationUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class CryptoObject implements EncryptionObject, DecryptionObject {
    
    
    public CryptoObject() {
        
    }
    
    // encryption
    
    @Override
    public <E extends Serializable> AESEncryptedObject<E> encryptObject(E object, Password password) throws EncryptionException {
        AESFactory aesFactory = new AESFactory();
        AESCipherFactory aesCipherFactory = new AESCipherFactory(aesFactory);
        
        try {
            Cipher cipher = aesCipherFactory.getCipher(password, Cipher.ENCRYPT_MODE);
            
            byte[] serializedObject = SerializationUtils.serialize(object);
            byte[] encryptedObject = cipher.doFinal(serializedObject);
            return new AESEncryptedObject<>(encryptedObject, aesCipherFactory);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException exception) {
            throw new EncryptionException(exception);
        }
    }
    
    @Override
    public <E extends Serializable> RSAEncryptedObject<E> encryptObject(E object, PublicKey publicKey) throws EncryptionException {
        RSAFactory rsaFactory = new RSAFactory();
        RSACipherFactory rsaCipherFactory = new RSACipherFactory(rsaFactory);
        try {
            Cipher cipher = rsaCipherFactory.getEncryptCipher(publicKey);
            
            byte[] serializedObject = SerializationUtils.serialize(object);
            byte[] encryptedObject = cipher.doFinal(serializedObject);
            return new RSAEncryptedObject<>(encryptedObject, rsaCipherFactory);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException exception) {
            throw new EncryptionException(exception);
        }
    }
    
    // decryption
    
    @Override
    public <E extends Serializable> E decryptObject(AESEncryptedObject<E> object, Password password) throws DecryptionException {
        if (!object.isEncrypted())
            throw new DecryptionException("Given object is not encrypted.");
        
        AESCipherFactory cipherFactory = object.getCipherFactory();
        try {
            Cipher cipher = cipherFactory.getCipher(password, Cipher.DECRYPT_MODE);
            
            byte[] decryptedObject = cipher.doFinal(object.getSerializedObject());
            return object.getOriginalObject(decryptedObject);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException exception) {
            throw new DecryptionException(exception);
        }
    }
    
    @Override
    public <E extends Serializable> E decryptObject(RSAEncryptedObject<E> object, PrivateKey privateKey) throws DecryptionException {
        if(!object.isEncrypted())
            throw new DecryptionException("Given object is not encrypted.");
        
        RSACipherFactory cipherFactory = object.getCipherFactory();
        try {
            Cipher cipher = cipherFactory.getDecryptCipher(privateKey);
            
            byte[] decryptedObject = cipher.doFinal(object.getSerializedObject());
            return object.getOriginalObject(decryptedObject);
        } catch (NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException exception) {
            throw new DecryptionException(exception);
        }
    }
    
}
