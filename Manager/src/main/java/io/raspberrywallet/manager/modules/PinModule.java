package io.raspberrywallet.manager.modules;

import io.raspberrywallet.RequiredInputNotFound;

import java.math.BigInteger;

public class PinModule extends Module {

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
}
