package io.raspberrywallet.manager.modules.pin;

import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.modules.Module;
import org.apache.commons.lang.SerializationUtils;

public class PinModule extends Module<PinConfig> {
    private static String PIN = "pin";

    public PinModule() throws InstantiationException, IllegalAccessException {
        super("Enter PIN", PinConfig.class);
    }

    public PinModule(Configuration.ModulesConfiguration modulesConfiguration) throws InstantiationException, IllegalAccessException {
        super("Enter PIN", modulesConfiguration, PinConfig.class);
    }

    @Override
    public String getDescription() {
        return "Module that require enter a digit code to unlock.";
    }


    @Override
    public void register() {
    }

    @Override
    public String getHtmlUi() {
        return "<input type=\"text\" name=\"pin\">";
    }

    @Override
    public byte[] encrypt(byte[] payload) throws EncryptionException {
        AESEncryptedObject<ByteWrapper> encryptedObject =
                CryptoObject.encrypt(new ByteWrapper(payload), getInput(PIN));

        return SerializationUtils.serialize(encryptedObject);
    }

    @Override
    public byte[] decrypt(byte[] payload) throws DecryptionException {
        AESEncryptedObject<ByteWrapper> encryptedObject =
                (AESEncryptedObject<ByteWrapper>) SerializationUtils.deserialize(payload);

        return CryptoObject.decrypt(encryptedObject, getInput(PIN)).getData();
    }

    @Override
    protected void validateInputs() throws RequiredInputNotFound {
        String pin = getInput(PIN);
        if (pin == null || pin.length() < configuration.minLength || pin.length() > configuration.maxLength)
            throw new RequiredInputNotFound(getId(), PIN);
    }


}
