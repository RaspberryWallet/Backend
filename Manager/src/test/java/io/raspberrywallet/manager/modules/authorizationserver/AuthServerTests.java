package io.raspberrywallet.manager.modules.authorizationserver;


import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServerTests {
    
    private AuthorizationServerModule module = new AuthorizationServerModule();
    
    @Test
    public void WalletEncryptionAndDecryptionWorks() {
        byte[] encryptionData = "secret data".getBytes();
        byte[] ignored = new byte[1];
    
        try {
            module.encrypt(encryptionData);
            byte[] decryptedData = module.decrypt(ignored);
            
            assertTrue(Arrays.equals(decryptedData, encryptionData));
            
        } catch (EncryptionException | DecryptionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void MultipleEncryptionAndDecryptionOperationWorks() {
        byte[] encryptionData = "secret data".getBytes();
        byte[] differentEncryptionData = "different secret data".getBytes();
        byte[] ignored = new byte[1];
    
        try {
            module.encrypt(encryptionData);
            module.encrypt(differentEncryptionData);
            byte[] decryptedData = module.decrypt(ignored);
            
            assertTrue(Arrays.equals(differentEncryptionData, decryptedData));
            assertFalse(Arrays.equals(encryptionData, decryptedData));
            
        } catch (EncryptionException | DecryptionException e) {
            fail(e.getMessage());
        }
    }
    
}
