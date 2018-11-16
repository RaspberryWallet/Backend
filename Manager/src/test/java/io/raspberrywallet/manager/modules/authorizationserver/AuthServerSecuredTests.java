package io.raspberrywallet.manager.modules.authorizationserver;


import io.raspberrywallet.contract.InternalModuleException;
import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These tests needs running authorization server to pass!
 */
class AuthServerSecuredTests {
    
    private byte[] encryptionData = "secret data".getBytes();
    private byte[] differentEncryptionData = "different secret data".getBytes();
    
    private AuthorizationServerModule module;
    
    @BeforeEach
    public void initialize() throws IllegalAccessException, InstantiationException {
        module = new AuthorizationServerModule();
        module.setInput("password", "abadziaba123");
    }
    
    @Test

    public void Secured_WalletEncryptionAndDecryptionWorks() throws EncryptionException, DecryptionException, InternalModuleException, RequiredInputNotFound {
        byte[] data = module.encryptKeyPart(encryptionData);
        byte[] decryptedData = module.decryptKeyPart(data);
    
        assertTrue(Arrays.equals(decryptedData, encryptionData));
    }
    
    @Test
    void Secured_MultipleEncryptionAndDecryptionOperationWorks() throws EncryptionException, DecryptionException, RequiredInputNotFound, InternalModuleException {
        byte[] firstEncryptedData = module.encryptKeyPart(encryptionData);
        assertTrue(Arrays.equals(encryptionData, module.decryptKeyPart(firstEncryptedData)));

        byte[] secondEncryptedData = module.encryptKeyPart(differentEncryptionData);
        assertTrue(Arrays.equals(differentEncryptionData, module.decryptKeyPart(secondEncryptedData)));
    }
    
}
