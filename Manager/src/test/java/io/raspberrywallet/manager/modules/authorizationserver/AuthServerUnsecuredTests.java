package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.contract.InternalModuleException;
import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.commons.CustomConfig;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("Http is disabled for now.")
class AuthServerUnsecuredTests {
    
    private byte[] encryptionData = "secret data".getBytes();
    private byte[] differentEncryptionData = "different secret data".getBytes();
    
    private AuthorizationServerModule module;
    
    @BeforeEach
    void initialize() throws IllegalAccessException, IOException, InstantiationException {
        String unsecuredConfParams = "{ \"host\": \"http://localhost\" }";
        Configuration.ModulesConfiguration unsecuredConf =
                CustomConfig.getConfig("AuthorizationServerModule", unsecuredConfParams);
        module = new AuthorizationServerModule(unsecuredConf);
        module.setInput("password", "abadziaba123");
    }
    
    @Test
    void Unsecured_WalletEncryptionAndDecryptionWorks() throws EncryptionException, DecryptionException, RequiredInputNotFound, InternalModuleException {
        byte[] data = module.encryptKeyPart(encryptionData);
        byte[] decryptedData = module.decryptKeyPart(data);
        
        assertTrue(Arrays.equals(decryptedData, encryptionData));
    }
    
    @Test
    void Unsecured_MultipleEncryptionAndDecryptionOperationWorks() throws EncryptionException, DecryptionException, RequiredInputNotFound, InternalModuleException {
        byte[] firstEncryptedData = module.encryptKeyPart(encryptionData);
        assertTrue(Arrays.equals(encryptionData, module.decryptKeyPart(firstEncryptedData)));

        byte[] secondEncryptedData = module.encryptKeyPart(differentEncryptionData);
        assertTrue(Arrays.equals(differentEncryptionData, module.decryptKeyPart(secondEncryptedData)));
    }
    
}
