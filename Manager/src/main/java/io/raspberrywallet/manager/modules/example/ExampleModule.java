package io.raspberrywallet.manager.modules.example;


import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.modules.Module;
import org.jetbrains.annotations.NotNull;

public class ExampleModule extends Module<ExampleConfig> {
    public ExampleModule() throws InstantiationException, IllegalAccessException {
        super("Ready", ExampleConfig.class);
    }

    public ExampleModule(Configuration.ModulesConfiguration modulesConfiguration) throws InstantiationException, IllegalAccessException {
        super("Ready", modulesConfiguration, ExampleConfig.class);
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Module doesn't require any action";
    }


    public static final byte[] KEY = "EXAMPLEKEY".getBytes();


    @Override
    public String getHtmlUi() {
        return null;
    }

    /*
     * Before we can decrypt a keypart, we need an encrypted one
     */
    @Override
    protected byte[] encrypt(byte[] data) {
        byte[] r = data.clone();
        for (int i = 0; i < r.length; ++i)
            r[i] = (byte) (r[i] ^ KEY[i % KEY.length]);
        return r;
    }

    /*
     * We are processing (decrypting) the keypart with a KEY.
     */
    @Override
    protected byte[] decrypt(byte[] payload) throws DecryptionException {
        if (payload == null) throw new DecryptionException(DecryptionException.NO_DATA);

        byte[] r = payload.clone();

        for (int i = 0; i < r.length; ++i)
            r[i] = (byte) (r[i] ^ KEY[i % KEY.length]);

        return r;
    }

    @Override
    protected void validateInputs() {
    }
}
