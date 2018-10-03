package io.raspberrywallet.manager.modules;

public class PinModule extends Module {
    Integer pin = null;

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean check() {
        return pin != null;
    }

    @Override
    public void process() {

    }

    @Override
    public void register() {

    }

    @Override
    public String getHtmlUi() {
        return "<form action=\"/nextStep\" method=\"post\">\n" +
                "\tPIN:<br>\n" +
                "\t<input type=\"text\" name=\"pin\">\n" +
                "\t<input type=\"submit\" value=\"Submit\">\n" +
                "</form>";
    }

    @Override
    public byte[] encryptInput(byte[] data, Object... params) {
        return new byte[0];
    }
}
