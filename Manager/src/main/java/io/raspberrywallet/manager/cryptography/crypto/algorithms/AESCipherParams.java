package io.raspberrywallet.manager.cryptography.crypto.algorithms;


import io.raspberrywallet.manager.cryptography.common.Password;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class AESCipherParams extends CipherParams implements Serializable {
    
    private String hashAlgorithmName;
    private byte[] ivBytes;
    private byte[] keySalt;
    
    private int iterationsAmount;
    
    public AESCipherParams() {
        this(new AESParams());
    }
    
    AESCipherParams(AESParams algorithmFactory) {
        SecureRandom random = new SecureRandom();
        byte keySalt[] = new byte[16];
        ivBytes = new byte[16];
        random.nextBytes(keySalt);
        random.nextBytes(ivBytes);
        
        algorithmName = algorithmFactory.getAlgorithmName();
        algorithmFullName = algorithmFactory.getFullAlgorithmName();
        hashAlgorithmName = algorithmFactory.getHashAlgorithmName();
        this.keySalt = keySalt;
        keySize = algorithmFactory.getKeySize();
        iterationsAmount = algorithmFactory.getKeyHashIterationsAmount();
    }
    
    public Cipher getCipher(Password password, int cipherMode) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        
        Cipher cipher = Cipher.getInstance(algorithmFullName);
        
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(hashAlgorithmName);
        PBEKeySpec spec = new PBEKeySpec(password.getSecret(), keySalt, iterationsAmount, keySize);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), algorithmName);
        cipher.init(cipherMode, secret, ivParameterSpec);
        
        return cipher;
    }
    
    public String getHashAlgorithmName() {
        return hashAlgorithmName;
    }
    
    public byte[] getIvBytes() {
        return ivBytes;
    }
    
    public byte[] getKeySalt() {
        return keySalt;
    }
    
}
