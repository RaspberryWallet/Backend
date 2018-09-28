package io.raspberrywallet.manager.cryptography.crypto.algorithms;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.*;

public class RSACipherParams extends CipherParams implements Serializable {
    
    public RSACipherParams() {
        this(new RSAParams());
    }
    
    RSACipherParams(RSAParams algorithmFactory) {
        algorithmName = algorithmFactory.getAlgorithmName();
        algorithmFullName = algorithmFactory.getFullAlgorithmName();
        keySize = algorithmFactory.getKeySize();
    }
    
    public KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName);
        generator.initialize(keySize);
        return generator.genKeyPair();
    }
    
    public KeyPair getKeyPairDefault() {
        RSAParams rsaFactory = new RSAParams();
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(rsaFactory.getAlgorithmName());
            generator.initialize(rsaFactory.getKeySize());
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public Cipher getEncryptCipher(PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithmFullName);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher;
    }
    
    public Cipher getDecryptCipher(PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithmFullName);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher;
    }
    
}
