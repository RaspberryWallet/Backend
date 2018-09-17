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

public class CryptoStream implements EncryptionStream, DecryptionStream {
    
    
    final private InputStream inputStream;
    final private OutputStream outputStream;
    
    public CryptoStream(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
    
    private static void transferTo(InputStream inputStream, OutputStream outputStream) throws IOException {
        while (inputStream.available() > 0)
            outputStream.write(inputStream.read());
    }
    
    // encryption
    
    @Override
    public void encryptStream(Password password) throws EncryptionException {
        try {
            AESFactory aesAlgorithmData = new AESFactory();
            AESCipherFactory aesCipherFactory = new AESCipherFactory(aesAlgorithmData);
            Cipher cipher = aesCipherFactory.getCipher(password, Cipher.ENCRYPT_MODE);
    
            encryptStream(cipher);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException exception) {
            throw new EncryptionException(exception);
        }
    }
    
    @Override
    public void encryptStream(PublicKey publicKey) throws EncryptionException {
        RSACipherFactory cipherFactory = new RSACipherFactory(new RSAFactory());
        try {
            Cipher cipher = cipherFactory.getEncryptCipher(publicKey);
            
            encryptStream(cipher);
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new EncryptionException(exception);
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
