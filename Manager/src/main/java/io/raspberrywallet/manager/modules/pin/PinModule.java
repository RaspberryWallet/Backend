package io.raspberrywallet.manager.modules.pin;

import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.cryptography.common.Password;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoStream;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.modules.Module;
import org.apache.commons.lang.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PinModule extends Module<PinConfig> {
    
    public PinModule() throws InstantiationException, IllegalAccessException {
        super("Enter PIN", PinConfig.class);
    }

    public PinModule(Configuration.ModulesConfiguration modulesConfiguration) throws InstantiationException, IllegalAccessException {
        super("Enter PIN", modulesConfiguration, PinConfig.class);
    }

    @Override
    public String getDescription() {
        return "Module that require enter 4 digits code";
    }

    @Override
    public boolean check() {
        return hasInput("pin");
    }


    @Override
    public void register() {
    }

    @Override
    public String getHtmlUi() {
        return "<input type=\"text\" name=\"pin\">";
    }

    @Override
    public byte[] encrypt(byte[] payload) throws RequiredInputNotFound, EncryptionException {
        Password password = new Password(getPin());
    
        AESEncryptedObject<ByteWrapper> encryptedObject =
                CryptoObject.encrypt(new ByteWrapper(payload), password);
        
        return SerializationUtils.serialize(encryptedObject);
    }

    @Override
    public byte[] decrypt(byte[] payload) throws RequiredInputNotFound, DecryptionException {
        Password password = new Password(getPin());
    
        AESEncryptedObject<ByteWrapper> encryptedObject =
                (AESEncryptedObject<ByteWrapper>) SerializationUtils.deserialize(payload);
        
        return CryptoObject.decrypt(encryptedObject, password).getData();
    }
    
    private String getPin() throws RequiredInputNotFound {
        String pin = getInput("pin");
        
        if (pin == null)
            throw new RequiredInputNotFound(getId(), "pin");
        
        return pin;
    }

    public static class Inputs {
        public static String PIN = "pin";
    }
}
