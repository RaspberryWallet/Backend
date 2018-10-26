package io.raspberrywallet.manager.modules.authorizationserver;


import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServerTests {
    
    private byte[] encryptionData = "secret data".getBytes();
    private byte[] differentEncryptionData = "different secret data".getBytes();
    
    private AuthorizationServerModule module;
    
    @BeforeEach
    void initializeModule() {
        module = new AuthorizationServerModule();
        module.setInput("password", "abadziaba123");
    }
    
    @Test
    void AuthorizationServerModuleInitializesCorrectly() {
        assertTrue(module.check());
    }
    
    @Test
    void WalletEncryptionAndDecryptionWorks() {
        try {
            assertTrue(module.check());
            
            byte[] data = module.encrypt(encryptionData);
            byte[] decryptedData = module.decrypt(data);
            
            assertTrue(Arrays.equals(decryptedData, encryptionData));
            
        } catch (EncryptionException | DecryptionException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    void MultipleEncryptionAndDecryptionOperationWorks() {
        try {
            assertTrue(module.check());
            
            byte[] firstEncryptedData = module.encrypt(encryptionData);
            assertTrue(Arrays.equals(encryptionData, module.decrypt(firstEncryptedData)));
            
            byte[] secondEncryptedData = module.encrypt(differentEncryptionData);
            assertTrue(Arrays.equals(differentEncryptionData, module.decrypt(secondEncryptedData)));
            
        } catch (EncryptionException | DecryptionException e) {
            fail(e.getMessage());
        }
    }
    
}
