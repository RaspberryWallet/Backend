package io.raspberrywallet.manager.cryptography.crypto;

import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.crypto.wrappers.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.common.Password;
import org.apache.commons.lang.SerializationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AESObjectsTests {
    
    private static final int randomObjectsAmount = 52;
    private static final Password defaultPassword = new Password("TestPassword123!@##$%".toCharArray());
    
    private static CryptoObject cryptoObject = new CryptoObject();
    private static Random random = new Random();
    private static byte[][] arrayOfRandomSizeObjects;
    private static Password[] arrayOfRandomPasswords;
    
    @BeforeAll
    static void initializeData() {
        arrayOfRandomSizeObjects = new byte[randomObjectsAmount][];
        
        for (int i = 0; i < randomObjectsAmount; i++) {
            arrayOfRandomSizeObjects[i] = new byte[random.nextInt(randomObjectsAmount * 4) + 1];
            random.nextBytes(arrayOfRandomSizeObjects[i]);
        }
        
        arrayOfRandomPasswords = new Password[randomObjectsAmount];
        for (int i = 0; i < randomObjectsAmount; i++) {
            int passwordSize = random.nextInt(256 - 4) + 4;
            byte[] passwordBytes = new byte[passwordSize];
            random.nextBytes(passwordBytes);
            arrayOfRandomPasswords[i] = new Password(TestsHelper.toCharArray(passwordBytes));
        }
    }
    
    @Test
    void SimpleObjectEncryptionAndDecryption_DoesNotThrowExceptions() {
        ByteWrapper wrappedData = new ByteWrapper(getRandomData());
        Password randomPassword = getRandomPassword();
        try {
            AESEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encrypt(wrappedData, defaultPassword);
    
            assertThrows(DecryptionException.class, () ->{
                ByteWrapper decryptedObject = cryptoObject.decrypt(encryptedObject, randomPassword);
            });
            
        } catch (EncryptionException e) {
            fail("Decryption failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    void WhenEncrypting_DataDecryptedWithDifferentPasswordDoesNotEquals() {
        ByteWrapper wrappedData = new ByteWrapper(getRandomData());
        try {
            AESEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encrypt(wrappedData, defaultPassword);
            ByteWrapper decryptedObject = cryptoObject.decrypt(encryptedObject, defaultPassword);
            
            assertNotEquals(decryptedObject, wrappedData);
            
        } catch (EncryptionException e) {
            fail("Encryption of an object failed with exception: " + e.getMessage());
        } catch (DecryptionException e) {
            fail("Decryption of an object failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    void ObjectDoesNotChange_WhenEncryptedAndDecrypted() {
        ByteWrapper wrappedData = new ByteWrapper(getRandomData());
        try {
            AESEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encrypt(wrappedData, defaultPassword);
            ByteWrapper decryptedObject = cryptoObject.decrypt(encryptedObject, defaultPassword);
            Assertions.assertEquals(wrappedData, decryptedObject);

        } catch (EncryptionException | DecryptionException e) {
            fail("Exception caught during encryption/decryption: " + e.getMessage());
        }
    }
    
    @Test
    void WhenEncryptingObjectsWithRandomSize_DataEqualsAndEncryptedObjectsChanges() {
        for (int i = 0; i < randomObjectsAmount; i++) {
            ByteWrapper data = new ByteWrapper(arrayOfRandomSizeObjects[i]);
            byte[] serializedWrappedData = SerializationUtils.serialize(data);
    
            try {
                AESEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encrypt(data, defaultPassword);
                assertEquals(serializedWrappedData, encryptedObject.getSerializedObject());
    
                ByteWrapper decryptObject = cryptoObject.decrypt(encryptedObject, defaultPassword);
                assertNotEquals(decryptObject, data);
                
            } catch (EncryptionException e) {
                fail("Encryption of an object with random size of " + arrayOfRandomSizeObjects[i].length
                        + " bytes failed with exception: " + e.getMessage());
            } catch (DecryptionException e) {
                fail("Decryption of an object with random size of " + arrayOfRandomSizeObjects[i].length
                        + " bytes failed with exception: " + e.getMessage());
            }
        }
    }
    
    @Test
    void WhenEncryptingObjectsWithRandomPassword_DataEqualsAndNotExceptionsAreThrown() {
        for (int i = 0; i < randomObjectsAmount; i++) {
            ByteWrapper data = new ByteWrapper(getRandomData());
            byte[] serializedData = SerializationUtils.serialize(data);
            
            try {
                AESEncryptedObject<ByteWrapper> encryptedObject = cryptoObject.encrypt(data, arrayOfRandomPasswords[i]);
                assertEquals(encryptedObject.getSerializedObject(), serializedData);
                
                ByteWrapper decryptObject = cryptoObject.decrypt(encryptedObject, arrayOfRandomPasswords[i]);
                assertNotEquals(decryptObject, data);
                
            } catch (EncryptionException e) {
                fail("Encryption of an object with random password and data failed with exception: " + e.getMessage());
            } catch (DecryptionException e) {
                fail("Decryption of an object with random password and data failed with exception: " + e.getMessage());
            }
        }
    }
    
    private byte[] getRandomData() {
        return arrayOfRandomSizeObjects[random.nextInt(randomObjectsAmount)];
    }
    
    private Password getRandomPassword() {
        return arrayOfRandomPasswords[random.nextInt(randomObjectsAmount)];
    }
}
