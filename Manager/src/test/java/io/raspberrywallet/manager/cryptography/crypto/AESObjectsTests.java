package io.raspberrywallet.manager.cryptography.crypto;

import io.raspberrywallet.manager.common.generators.RandomStringGenerator;
import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.apache.commons.lang.SerializationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.Arrays;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AESObjectsTests {
    
    private static final int randomObjectsAmount = 52;
    private static final String defaultPassword = "TestPassword123!@##$%";
    
    private static Random random = new Random();
    private static byte[][] arrayOfRandomSizeObjects;
    private static String[] arrayOfRandomPasswords;
    
    @BeforeAll
    static void initializeData() {
        arrayOfRandomSizeObjects = new byte[randomObjectsAmount][];
        
        for (int i = 0; i < randomObjectsAmount; i++) {
            arrayOfRandomSizeObjects[i] = new byte[random.nextInt(randomObjectsAmount * 4) + 1];
            random.nextBytes(arrayOfRandomSizeObjects[i]);
        }

        arrayOfRandomPasswords = new String[randomObjectsAmount];
        for (int i = 0; i < randomObjectsAmount; i++) {
            int passwordSize = random.nextInt(256 - 4) + 4;
            arrayOfRandomPasswords[i] = RandomStringGenerator.get(passwordSize);
        }
    }
    
    @Test
    void WhenEncrypting_DataDecryptedWithDifferentPasswordDoesThrowException() {
        ByteWrapper wrappedData = new ByteWrapper(getRandomData());
        String randomPassword = getRandomPassword();
        try {
            AESEncryptedObject<ByteWrapper> encryptedObject = CryptoObject.encrypt(wrappedData, defaultPassword);
    
            assertThrows(DecryptionException.class, () -> {
                ByteWrapper decryptedObject = CryptoObject.decrypt(encryptedObject, randomPassword);
            });
            
        } catch (EncryptionException e) {
            fail("Encryption failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    void ObjectDoesNotChange_WhenEncryptedAndDecrypted() {
        ByteWrapper wrappedData = new ByteWrapper(getRandomData());
        try {
            AESEncryptedObject<ByteWrapper> encryptedObject = CryptoObject.encrypt(wrappedData, defaultPassword);
            ByteWrapper decryptedObject = CryptoObject.decrypt(encryptedObject, defaultPassword);
            assertEquals(wrappedData, decryptedObject);

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
                AESEncryptedObject<ByteWrapper> encryptedObject = CryptoObject.encrypt(data, defaultPassword);
                assertFalse(Arrays.areEqual(encryptedObject.getSerializedObject(), serializedWrappedData));
    
                ByteWrapper decryptObject = CryptoObject.decrypt(encryptedObject, defaultPassword);
                assertEquals(decryptObject, data);
                
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
                AESEncryptedObject<ByteWrapper> encryptedObject = CryptoObject.encrypt(data, arrayOfRandomPasswords[i]);
                assertFalse(Arrays.areEqual(serializedData, encryptedObject.getSerializedObject()));
                
                ByteWrapper decryptObject = CryptoObject.decrypt(encryptedObject, arrayOfRandomPasswords[i]);
                assertEquals(decryptObject, data);
                
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

    private String getRandomPassword() {
        return arrayOfRandomPasswords[random.nextInt(randomObjectsAmount)];
    }
}
