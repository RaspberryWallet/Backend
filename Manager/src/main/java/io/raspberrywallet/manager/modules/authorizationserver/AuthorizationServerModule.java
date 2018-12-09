package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.contract.InternalModuleException;
import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.common.generators.RandomStringGenerator;
import io.raspberrywallet.manager.common.readers.WalletUUIDReader;
import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.common.wrappers.Credentials;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.modules.Module;
import org.apache.commons.lang.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.UUID;

public class AuthorizationServerModule extends Module<AuthorizationServerConfig> {
    public static final String PASSWORD = "password";
    private final static int PASSWORD_SIZE_IN_BYTES = 256;

    private final WalletUUIDReader walletUUIDReader = WalletUUIDReader.getInstance();
    private final UUID walletUUID = walletUUIDReader.get();

    private final AuthorizationServerAPI serverAPI = new AuthorizationServerAPI(configuration);

    public AuthorizationServerModule() throws InstantiationException, IllegalAccessException {
        super("Please enter username and password", AuthorizationServerConfig.class);
    }

    public AuthorizationServerModule(Configuration.ModulesConfiguration modulesConfiguration) throws InstantiationException, IllegalAccessException {
        super("Please enter username and password", modulesConfiguration, AuthorizationServerConfig.class);
    }

    @Override
    protected void validateInputs() throws RequiredInputNotFound {
        if (!hasInput(PASSWORD))
            throw new RequiredInputNotFound(AuthorizationServerModule.class.getName(), PASSWORD);
    }

    private void callServer(Credentials serverCredentials) throws InternalModuleException {
        try {
            if (!serverAPI.isRegistered(serverCredentials))
                serverAPI.register(serverCredentials);

            if (!serverAPI.isLoggedIn())
                serverAPI.login(serverCredentials);

            if (!serverAPI.secretIsSet(serverCredentials)) {
                String randomSecret = RandomStringGenerator.get(PASSWORD_SIZE_IN_BYTES);
                serverAPI.overwriteSecret(serverCredentials, randomSecret);
            }
        } catch (RequestException e) {
            throw new InternalModuleException(
                    "Request exception in module " + AuthorizationServerConfig.class.getName() + ":" + e.getMessage());
        }
    }

    @NotNull
    private Credentials createCredentials(String password) {
        return new Credentials(walletUUID.toString(),
                Base64.getUrlEncoder().encodeToString(password.getBytes()));
    }

    @Override
    protected byte[] encrypt(byte[] payload) throws EncryptionException, InternalModuleException {
        try {
            Credentials serverCredentials = createCredentials(getInput(PASSWORD));
            callServer(serverCredentials);
            String password = serverAPI.getSecret(serverCredentials);

            AESEncryptedObject<ByteWrapper> encryptedSecret =
                    CryptoObject.encrypt(new ByteWrapper(payload), password);

            return SerializationUtils.serialize(encryptedSecret);

        } catch (RequestException e) {
            throw new EncryptionException(e.getMessage());
        }
    }

    @Override
    protected byte[] decrypt(byte[] keyPart) throws DecryptionException, InternalModuleException {
        try {
            Credentials serverCredentials = createCredentials(getInput(PASSWORD));
            callServer(serverCredentials);
            String password = serverAPI.getSecret(serverCredentials);

            AESEncryptedObject<ByteWrapper> deserializedKeyPart =
                    (AESEncryptedObject<ByteWrapper>) SerializationUtils.deserialize(keyPart);

            return CryptoObject.decrypt(deserializedKeyPart, password).getData();

        } catch (RequestException e) {
            throw new InternalModuleException(e.getMessage());
        }
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Module authenticates user with external authorization server.";
    }

    @Nullable
    @Override
    public String getHtmlUi() {
        return "<input type=\"text\" name=\"password\">";
    }

}
