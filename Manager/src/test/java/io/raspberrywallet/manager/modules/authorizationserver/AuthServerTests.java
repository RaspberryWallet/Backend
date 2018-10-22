package io.raspberrywallet.manager.modules.authorizationserver;


import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServerTests {
    
    private AuthorizationServerModule module = new AuthorizationServerModule();
    private byte[] encryptionData = "secret data".getBytes();
    
    @Test
    public void WalletEncryptionAndDecryptionWorks() {
        try {
            byte[] ignored = module.encrypt(encryptionData);
            byte[] decryptedData = module.decrypt(ignored);
            assertTrue(Arrays.equals(decryptedData, encryptionData));
        } catch (EncryptionException | DecryptionException e) {
            fail(e.getMessage());
        }
    }
    
}
