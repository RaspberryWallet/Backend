package io.raspberrywallet.manager.modules.pin;

import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.modules.Module;

import java.math.BigInteger;

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
    public byte[] encrypt(byte[] payload) throws RequiredInputNotFound {
        return crypt(payload);
    }

    @Override
    public byte[] decrypt(byte[] payload) throws RequiredInputNotFound {
        return crypt(payload);
    }


    private byte[] crypt(byte[] payload) throws RequiredInputNotFound {
        BigInteger bigData = new BigInteger(payload);
        String pin = getInput("pin");
        if (pin == null) throw new RequiredInputNotFound(getId(), "pin");
        BigInteger bigPin = new BigInteger(pin);
        return bigData.xor(bigPin).toByteArray();
    }

    public static class Inputs {
        public static String PIN = "pin";
    }
}
