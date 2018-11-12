package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.common.readers.WalletUUIDReader;
import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.common.wrappers.Credentials;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.modules.Module;
import org.apache.commons.lang.SerializationUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.Random;
import java.util.UUID;

public class AuthorizationServerModule extends Module<AuthorizationServerConfig> {

    private final static int PASSWORD_SIZE_IN_BYTES = 32;

    private final WalletUUIDReader walletUUIDReader = WalletUUIDReader.getInstance();
    private final UUID walletUUID = walletUUIDReader.get();

    //todo change it to read data from conf yml
    private Credentials serverCredentials = new Credentials(walletUUID.toString(), "123");

    private final AuthorizationServerAPI serverAPI = new AuthorizationServerAPI(configuration);

    private Random random = new Random();

    public AuthorizationServerModule() throws InstantiationException, IllegalAccessException {
        super("Please enter username and password for external server.", AuthorizationServerConfig.class);
    }

    public AuthorizationServerModule(Configuration.ModulesConfiguration modulesConfiguration) throws InstantiationException, IllegalAccessException {
        super("Please enter username and password for external server.", modulesConfiguration, AuthorizationServerConfig.class);
    }

    @Override
    public String getDescription() {
        return "This module is authenticating user with external authorization server.";
    }

    @Override
    public boolean check() {
        if (hasInput("password")) {
            try {
                String password = getInput("password");
                serverCredentials = new Credentials(walletUUID.toString(), password);
                initialize();
            } catch (RequestException e) {
                return false;
            }
            return true;
        } else return serverCredentials != null;
    }


    private void initialize() throws RequestException {
        if (!serverAPI.isRegistered(serverCredentials))
            serverAPI.register(serverCredentials);

        if (!serverAPI.isLoggedIn())
            serverAPI.login(serverCredentials);

        if (!serverAPI.secretIsSet(serverCredentials)) {
            String randomSecret = getRandomString();
            String encodedSecret = Base64.getEncoder().encodeToString(randomSecret.getBytes());
            serverAPI.overwriteSecret(serverCredentials, encodedSecret);
        }
    }

    private String getRandomString() {
        byte[] randomBytes = new byte[PASSWORD_SIZE_IN_BYTES];
        random.nextBytes(randomBytes);
        return new String(randomBytes);
    }

    @Override
    public byte[] encrypt(byte[] payload) throws EncryptionException {
        try {
            initialize();

            String encodedSecret = serverAPI.getSecret(serverCredentials);
            String password = new String(Base64.getDecoder().decode(encodedSecret));

            AESEncryptedObject<ByteWrapper> encryptedSecret =
                    CryptoObject.encrypt(new ByteWrapper(payload), password);

            return SerializationUtils.serialize(encryptedSecret);

        } catch (RequestException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    @Override
    public byte[] decrypt(byte[] keyPart) throws DecryptionException {
        try {
            String encodedSecret = serverAPI.getSecret(serverCredentials);
            String password = new String(Base64.getDecoder().decode(encodedSecret));

            AESEncryptedObject<ByteWrapper> deserializedKeyPart =
                    (AESEncryptedObject<ByteWrapper>) SerializationUtils.deserialize(keyPart);

            return CryptoObject.decrypt(deserializedKeyPart, password).getData();

        } catch (RequestException e) {
            throw new DecryptionException(e.getMessage());
        }
    }

    /**
     * There is no need to take actions before next encryption/decryption
     * operations, so there is no point in implementing this method.
     */
    @Override
    public void register() {
    }

    @Nullable
    @Override
    public String getHtmlUi() {
        return "<input type=\"text\" name=\"password\">";
    }


}
