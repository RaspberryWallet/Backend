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
import org.jetbrains.annotations.NotNull;

public class PinModule extends Module<PinConfig> {
    public static final String PIN = "pin";

    public PinModule() throws InstantiationException, IllegalAccessException {
        super("Enter PIN", PinConfig.class);
    }

    public PinModule(Configuration.ModulesConfiguration modulesConfiguration) throws InstantiationException, IllegalAccessException {
        super("Enter PIN", modulesConfiguration, PinConfig.class);
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Module requires enter a digit code to unlock.";
    }

    @Override
    public String getHtmlUi() {
        return "<input type=\"text\" name=\"pin\">";
    }

    @Override
    protected byte[] encrypt(byte[] payload) throws EncryptionException {
        AESEncryptedObject<ByteWrapper> encryptedObject =
                CryptoObject.encrypt(new ByteWrapper(payload), getInput(PIN));

        return SerializationUtils.serialize(encryptedObject);
    }

    @Override
    protected byte[] decrypt(byte[] payload) throws DecryptionException {
        AESEncryptedObject<ByteWrapper> encryptedObject =
                (AESEncryptedObject<ByteWrapper>) SerializationUtils.deserialize(payload);

        return CryptoObject.decrypt(encryptedObject, getInput(PIN)).getData();
    }

    @Override
    protected void validateInputs() throws RequiredInputNotFound {
        String pin = getInput(PIN);
        if (isPinWeak(pin))
            throw new RequiredInputNotFound(getId(), PIN);
    }

    private boolean isPinWeak(String pin) {
        return pin == null || pin.length() < configuration.minLength || pin.length() > configuration.maxLength;
    }
}
