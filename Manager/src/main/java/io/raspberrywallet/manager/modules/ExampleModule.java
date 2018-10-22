package io.raspberrywallet.manager.modules;

import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;

public class ExampleModule extends Module {

    @Override
    public String getDescription() {
        return "An example waiting and xoring module to show how things work.";
    }

    public static final byte[] KEY = "EXAMPLEKEY".getBytes();

    /*
     * First the module is "registered" by manager just after user needs to decrypt keypart.
     */
    private long lastTime = 1000;

    @Override
    public void register() {
        lastTime = System.currentTimeMillis();
        setStatusString("Wait 5 seconds for decryption to start");
    }

    @Override
    public String getHtmlUi() {
        return null;
    }

    /*
     * The manager checks periodically for status. If status is true (i.e. here 5 seconds passed),
     * process() should be called
     */
    @Override
    public boolean check() {
        return System.currentTimeMillis() - lastTime > 5000;
    }

    /*
     * Before we can decrypt a keypart, we need an encrypted one
     */
    @Override
    public byte[] encrypt(byte[] data) {
        byte[] r = data.clone();
        for (int i = 0; i < r.length; ++i)
            r[i] = (byte) (r[i] ^ KEY[i % KEY.length]);
        return r;
    }

    /*
     * We are processing (decrypting) the keypart with a KEY.
     */
    @Override
    public byte[] decrypt(byte[] payload) throws DecryptionException {
        if (payload == null) throw new DecryptionException(DecryptionException.getNO_DATA());

        byte[] r = payload.clone();

        for (int i = 0; i < r.length; ++i)
            r[i] = (byte) (r[i] ^ KEY[i % KEY.length]);

        return r;
    }
}
