package io.raspberrywallet.manager.cryptography.crypto;

import io.raspberrywallet.manager.cryptography.crypto.algorithms.AESCipherFactory;
import io.raspberrywallet.manager.cryptography.crypto.algorithms.RSACipherFactory;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.common.Password;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * Class that is used for Stream encryption and decryption.
 * It keeps instances of streams to encrypt or decrypt.
 */
public class CryptoStream {
    
    final private InputStream inputStream;
    final private OutputStream outputStream;
    
    /**
     * Pass streams that are going to be encrypted or decrypted.
     * @param inputStream From this stream, the encryption or decryption operation will fetch the data.
     * @param outputStream To this stream, the encryption or decryption operation will save the data.
     */
    public CryptoStream(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
    
    /**
     * Method transferTo was added in Java 9 and it's simple work around to keep with Java 8.
     */
    private static void transferTo(InputStream inputStream, OutputStream outputStream) throws IOException {
        while (inputStream.available() > 0)
            outputStream.write(inputStream.read());
    }
    
    // encryption
    
    /**
     * This method is using AES algorithm to encrypt given input stream and write it to given output stream.
     * Output stream contain serialized header with metadata, that can be used for decryption.
     * @param password Password that will be used with PBEKeySpec for AES encryption.
     * @throws EncryptionException If there is any error with encryption, then it's caught and thrown as
     *                             EncryptionException, with original or custom error message.
     */
    public void encrypt(Password password) throws EncryptionException {
        try {
            AESCipherFactory aesCipherFactory = new AESCipherFactory();
            Cipher cipher = aesCipherFactory.getCipher(password, Cipher.ENCRYPT_MODE);
    
            encrypt(cipher);
        }
        catch (IOException | InvalidKeyException e) {
            throw new EncryptionException(e);
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
            // this shouldn't happen, if so, there is serious problem with system
            throw new RuntimeException(e);
        }
    }
    
    /**
     * This method is using AES algorithm to encrypt given input stream and write it to given output stream.
     * Output stream contains serialized header with metadata, that can be used for decryption.
     * @param publicKey Public key used for encryption.
     */
    public void encrypt(PublicKey publicKey) throws EncryptionException {
        RSACipherFactory cipherFactory = new RSACipherFactory();
        try {
            Cipher cipher = cipherFactory.getEncryptCipher(publicKey);
            
            encrypt(cipher);
        }
        catch (IOException | InvalidKeyException exception) {
            throw new EncryptionException(exception);
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            // this shouldn't happen, if so, there is serious problem with system
            throw new RuntimeException(e);
        }
    }
    
    private void encrypt(Cipher cipher) throws IOException {
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
            transferTo(inputStream, cipherOutputStream);
        }
    }
    
    // decryption
    
    /**
     * This method is used for decryption of encrypted data in given input stream. The encryption algorithm must be AES.
     * The input stream must contain AESFactory serialized header, which was used to encryption.
     * @param password Password that will be used for creating PBBKeySpec for AES decryption.
     * @throws DecryptionException If there is any error in decryption, then it is caught and thrown as
     *                             DecryptionException, with it's original or custom message.
     */
    public void decrypt(Password password) throws DecryptionException {
        try {
            AESCipherFactory aesCipherFactory = CipherHeaderManager.readCipherData(inputStream);
            Cipher cipher = aesCipherFactory.getCipher(password, Cipher.DECRYPT_MODE);
            decrypt(cipher);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException exception) {
            throw new DecryptionException(exception);
        }
    }
    
    /**
     * This method is used for decryption of encrypted data in given input stream. The encryption algorithm must be RSA.
     * The input stream must contain RSAFactory serialized header, which was used to encryption.
     * @param privateKey Private RSA key needed for decryption.
     * @throws DecryptionException If there is any error in decryption, then it is caught and thrown as
     *                             DecryptionException, with it's original or custom message.
     */
    public void decrypt(PrivateKey privateKey) throws DecryptionException {
        try {
            RSACipherFactory rsaCipherFactory = CipherHeaderManager.readCipherData(inputStream);
            Cipher cipher = rsaCipherFactory.getDecryptCipher(privateKey);
            
            decrypt(cipher);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException exception) {
            throw new DecryptionException(exception);
        }
    }
    
    private void decrypt(Cipher cipher) throws IOException {
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
             transferTo(inputStream, cipherOutputStream);
        }
    }
}
