package io.raspberrywallet.manager.modules.pin;

import io.raspberrywallet.contract.InternalModuleException;
import io.raspberrywallet.contract.ModuleInitializationException;
import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.modules.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinModuleTests {
    
    private final static String data = "secret data for encryption";
    
    private Module<PinConfig> pinModule;
    
    @BeforeEach
    public void initializeModule() throws IllegalAccessException, InstantiationException, ModuleInitializationException {
        pinModule = new PinModule();
    }
    
    @Test
    public void PinModuleConstructorDoesNotThrow() throws IllegalAccessException, InstantiationException, ModuleInitializationException {
        Module<PinConfig> pinModule = new PinModule();
    }

    
    @Test
    public void PinModuleEncryptsAndDecryptsCorrectly() throws EncryptionException, RequiredInputNotFound, DecryptionException, InternalModuleException {
        pinModule.setInput("pin", "1234");

        byte[] encryptedData = pinModule.encryptKeyPart(data.getBytes());
        byte[] decryptedData = pinModule.decryptKeyPart(encryptedData);
        
        assertTrue(Arrays.equals(data.getBytes(), decryptedData));
    }
    
    @Test
    public void DecryptionDoesThrowWithWrongPin() throws EncryptionException, RequiredInputNotFound, InternalModuleException {
        pinModule.setInput("pin", "1234");

        byte[] encryptedData = pinModule.encryptKeyPart(data.getBytes());
        pinModule.clearInputs();
        pinModule.setInput("pin","4567");
        assertThrows(DecryptionException.class, () -> pinModule.decryptKeyPart(encryptedData));
    }
    
}
