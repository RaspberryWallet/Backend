package io.raspberrywallet.manager.cryptography.crypto;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.*;

class RSACipherFactory extends CipherFactory implements Serializable {
    
    RSACipherFactory(AlgorithmFactory algorithmFactory) {
        RSAFactory rsaAlgorithmData = (RSAFactory)algorithmFactory;
        
        algorithmName = rsaAlgorithmData.getAlgorithmName();
        algorithmFullName = rsaAlgorithmData.getFullAlgorithmName();
        keySize = rsaAlgorithmData.getKeySize();
    }
    
    public KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName);
        generator.initialize(keySize);
        return generator.genKeyPair();
    }
    
    public KeyPair getKeyPairDefault() {
        RSAFactory rsaFactory = new RSAFactory();
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(rsaFactory.getAlgorithmName());
            generator.initialize(rsaFactory.getKeySize());
            return generator.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    Cipher getEncryptCipher(PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithmFullName);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher;
    }
    
    Cipher getDecryptCipher(PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(algorithmFullName);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher;
    }
    
}
