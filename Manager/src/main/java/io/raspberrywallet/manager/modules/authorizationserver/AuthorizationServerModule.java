package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.contract.ModuleInitializationException;
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
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

public class AuthorizationServerModule extends Module<AuthorizationServerConfig> {
    public static final String PASSWORD = "password";
    private final static int PASSWORD_SIZE_IN_BYTES = 256;

    private final WalletUUIDReader walletUUIDReader = WalletUUIDReader.getInstance();
    private final UUID walletUUID = walletUUIDReader.get();

    //todo change it to read data from conf yml
    private Credentials serverCredentials = new Credentials(walletUUID.toString(), "123");

    private final AuthorizationServerAPI serverAPI = new AuthorizationServerAPI(configuration);

    private Random random = new SecureRandom();

    public AuthorizationServerModule() throws InstantiationException, IllegalAccessException {
        super("Please enter username and password for external server.", AuthorizationServerConfig.class);
        tryGetInputsAndInitialize();
    }

    public AuthorizationServerModule(Configuration.ModulesConfiguration modulesConfiguration) throws InstantiationException, IllegalAccessException {
        super("Please enter username and password for external server.", modulesConfiguration, AuthorizationServerConfig.class);
        tryGetInputsAndInitialize();
    }
    
    @Override
    protected void validateInputs() throws RequiredInputNotFound {
        if (hasInput(PASSWORD)) {
            String password = getInput(PASSWORD);
            if (password != null)
                serverCredentials = new Credentials(walletUUID.toString(),
                        Base64.getUrlEncoder().encodeToString(password.getBytes()));
        }
        else if (serverCredentials == null)
            throw new RequiredInputNotFound(AuthorizationServerModule.class.getName(), "password");
    }
    
    private void initialize() throws ModuleInitializationException {
        try {
            validateInputs();
            
            if (!serverAPI.isRegistered(serverCredentials))
                serverAPI.register(serverCredentials);
            
            if (!serverAPI.isLoggedIn())
                serverAPI.login(serverCredentials);
            
            if (!serverAPI.secretIsSet(serverCredentials)) {
                String randomSecret = RandomStringGenerator.get(PASSWORD_SIZE_IN_BYTES);
                serverAPI.overwriteSecret(serverCredentials, randomSecret);
            }
        } catch (RequestException | RequiredInputNotFound e) {
            throw new ModuleInitializationException(
                    "Failed to initialize module " + AuthorizationServerConfig.class.getName() + ":" + e.getMessage());
        }
    }
    
    private void tryGetInputsAndInitialize() {
        // if it's possible, try to get inputs and initialize module
        try {
            validateInputs();
            initialize();
        } catch (RequiredInputNotFound | ModuleInitializationException ignored) {
        
        }
    }
    
    @Override
    public byte[] encrypt(byte[] payload) throws EncryptionException, RequiredInputNotFound {
        try {
            initialize();

            String password = serverAPI.getSecret(serverCredentials);

            AESEncryptedObject<ByteWrapper> encryptedSecret =
                    CryptoObject.encrypt(new ByteWrapper(payload), password);

            return SerializationUtils.serialize(encryptedSecret);

        } catch (RequestException e) {
            throw new EncryptionException(e.getMessage());
        } catch (ModuleInitializationException e) {
            throw new RequiredInputNotFound(AuthorizationServerModule.class.getName()
                            + " has no required inputs or failed on initialization phase. Message: "
                            + e.getMessage());
        }
    }
    
    @Override
    public byte[] decrypt(byte[] keyPart) throws DecryptionException, RequiredInputNotFound {
        try {
            initialize();
    
            String password = serverAPI.getSecret(serverCredentials);

            AESEncryptedObject<ByteWrapper> deserializedKeyPart =
                    (AESEncryptedObject<ByteWrapper>) SerializationUtils.deserialize(keyPart);

            return CryptoObject.decrypt(deserializedKeyPart, password).getData();

        } catch (RequestException e) {
            throw new DecryptionException(e.getMessage());
        } catch (ModuleInitializationException e) {
            throw new RequiredInputNotFound(AuthorizationServerModule.class.getName()
                    + " has no required inputs or failed on initialization phase. Message: "
                    + e.getMessage());
        }
    }
    
    @Override
    public String getDescription() {
        return "This module is authenticating user with external authorization server.";
    }
    
    @Nullable
    @Override
    public String getHtmlUi() {
        return "<input type=\"text\" name=\"password\">";
    }
    
}
