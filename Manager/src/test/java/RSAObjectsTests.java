import io.raspberrywallet.manager.cryptography.ciphers.RSACipherFactory;
import io.raspberrywallet.manager.cryptography.ciphers.RSAFactory;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.wrappers.crypto.RSAEncryptedObject;
import org.apache.commons.lang.SerializationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class RSAObjectsTests {
    
    private static final int randomObjectsAmount = 256;
    
    private static final RSACipherFactory rsaCipherFactory = new RSACipherFactory(new RSAFactory());
    private static final KeyPair defaultKeyPair = rsaCipherFactory.getKeyPairDefault();
    private static final CryptoObject cryptoObject = new CryptoObject();
    private static final Random random = new Random();
    private static byte[][] arrayOfRandomData;
    
    // in bytes
    private static final int maxDataSize = 123;
    
    @BeforeAll
    static void initializeData() {
        arrayOfRandomData = new byte[randomObjectsAmount][];
        
        for (int i = 0; i < randomObjectsAmount; i++) {
            arrayOfRandomData[i] = new byte[random.nextInt(1) + maxDataSize];
            random.nextBytes(arrayOfRandomData[i]);
        }
    }
    
    @Test
    void WhenEncryptingData_DataEqualsAfterDecryptionAndNotExceptionIsThrown() {
        ByteWrapper data = new ByteWrapper(getRandomData());
        byte[] serializedData = SerializationUtils.serialize(data);
        
        try {
             RSAEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encryptObject(data, defaultKeyPair.getPublic());
             assertNotEquals(encryptedObject.getCipherFactory(), serializedData);
             
             ByteWrapper decryptedObject = cryptoObject.decryptObject(encryptedObject, defaultKeyPair.getPrivate());
             assertEquals(decryptedObject, data);
             
        } catch (EncryptionException e) {
            fail("Encryption failed with exception: " + e.getMessage());
        } catch (DecryptionException e) {
            fail("Decryption failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    void WhenEncryptingRandomData_DataEqualsAfterDecryption() {
        for (int i = 0; i < randomObjectsAmount; i++) {
            ByteWrapper data = new ByteWrapper(arrayOfRandomData[i]);
            byte[] serializedData = SerializationUtils.serialize(data);
        
            try {
                RSAEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encryptObject(data, defaultKeyPair.getPublic());
                assertNotEquals(encryptedObject.getCipherFactory(), serializedData);
            
                ByteWrapper decryptedObject = cryptoObject.decryptObject(encryptedObject, defaultKeyPair.getPrivate());
                assertEquals(decryptedObject, data);
            
            } catch (EncryptionException e) {
                fail("Encryption failed with exception: " + e.getMessage());
            } catch (DecryptionException e) {
                fail("Decryption failed with exception: " + e.getMessage());
            }
        }
    }
    
    @Test
    void WhenDecryptingDataWithWrongKey_ExceptionIsThrown() {
        ByteWrapper data = new ByteWrapper(getRandomData());
        KeyPair newKeyPair = rsaCipherFactory.getKeyPairDefault();
        
        try {
            RSAEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encryptObject(data, defaultKeyPair.getPublic());
            
            assertThrows(DecryptionException.class, () -> {
                ByteWrapper decryptedObject = cryptoObject.decryptObject(encryptedObject, newKeyPair.getPrivate());
            });
        } catch (EncryptionException e) {
            fail("Encryption failed with exception: " + e.getMessage());
        }
    }
    
    private byte[] getRandomData() {
        return arrayOfRandomData[random.nextInt(randomObjectsAmount)];
    }
    
}
