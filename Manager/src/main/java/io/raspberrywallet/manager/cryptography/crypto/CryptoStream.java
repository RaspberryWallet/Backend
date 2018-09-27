package io.raspberrywallet.manager.cryptography.crypto;

import io.raspberrywallet.manager.cryptography.ciphers.AESCipherFactory;
import io.raspberrywallet.manager.cryptography.ciphers.AESFactory;
import io.raspberrywallet.manager.cryptography.ciphers.RSACipherFactory;
import io.raspberrywallet.manager.cryptography.ciphers.RSAFactory;
import io.raspberrywallet.manager.cryptography.crypto.decryption.DecryptionStream;
import io.raspberrywallet.manager.cryptography.crypto.encryption.EncryptionStream;
import io.raspberrywallet.manager.cryptography.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.wrappers.data.Password;

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
public class CryptoStream implements EncryptionStream, DecryptionStream {
    
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
     * This method is going to encrypt given input stream with given password and transfer it
     * to output stream.
     * @param password Password that will be used with PBEKeySpec for AES encryption.
     * @throws EncryptionException This exception will be thrown, if there is IO problem or given password is wrong.
     */
    @Override
    public void encryptStream(Password password) throws EncryptionException {
        try {
            AESFactory aesAlgorithmData = new AESFactory();
            AESCipherFactory aesCipherFactory = new AESCipherFactory(aesAlgorithmData);
            Cipher cipher = aesCipherFactory.getCipher(password, Cipher.ENCRYPT_MODE);
    
            encryptStream(cipher);
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
     * This method is going to encrypt given input stream with given RSA public key and transfer it
     * to output stream.
     * @param publicKey Public key used for encryption.
     * @throws EncryptionException
     */
    @Override
    public void encryptStream(PublicKey publicKey) throws EncryptionException {
        RSACipherFactory cipherFactory = new RSACipherFactory(new RSAFactory());
        try {
            Cipher cipher = cipherFactory.getEncryptCipher(publicKey);
            
            encryptStream(cipher);
        }
        catch (IOException | InvalidKeyException exception) {
            throw new EncryptionException(exception);
        }
        catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            // this shouldn't happen, if so, there is serious problem with system
            throw new RuntimeException(e);
        }
    }
    
    private void encryptStream(Cipher cipher) throws IOException {
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
            transferTo(inputStream, cipherOutputStream);
        }
    }
    
    // decryption
    
    @Override
    public void decryptStream(Password password) throws DecryptionException {
        try {
            AESCipherFactory aesCipherFactory = CipherHeaderManager.readCipherData(inputStream);
            Cipher cipher = aesCipherFactory.getCipher(password, Cipher.DECRYPT_MODE);
            decryptStream(cipher);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException exception) {
            throw new DecryptionException(exception);
        }
    }
    
    @Override
    public void decryptStream(PrivateKey privateKey) throws DecryptionException {
        try {
            RSACipherFactory rsaCipherFactory = CipherHeaderManager.readCipherData(inputStream);
            Cipher cipher = rsaCipherFactory.getDecryptCipher(privateKey);
            
            decryptStream(cipher);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IOException exception) {
            throw new DecryptionException(exception);
        }
    }
    
    private void decryptStream(Cipher cipher) throws IOException {
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
             transferTo(inputStream, cipherOutputStream);
        }
    }
}
