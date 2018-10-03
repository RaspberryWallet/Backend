package io.raspberrywallet.manager.modules;

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
    public void process() {
        decrypt(payload -> {
            BigInteger bigData = new BigInteger(payload);
            BigInteger bigPin = new BigInteger(getInput("pin"));
            return bigData.xor(bigPin).toByteArray();
        });
    }

    @Override
    public void register() {

    }

    @Override
    public String getHtmlUi() {
        return "<input type=\"text\" name=\"pin\">";
    }

    @Override
    public byte[] encryptInput(byte[] data) {
        BigInteger bigData = new BigInteger(data);
        BigInteger bigPin = new BigInteger(getInput("pin"));

        return bigData.xor(bigPin).toByteArray();
    }
}
