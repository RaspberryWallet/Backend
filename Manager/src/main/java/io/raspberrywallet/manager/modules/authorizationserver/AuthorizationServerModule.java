package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.manager.common.readers.StringReader;
import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.common.wrappers.Credentials;
import io.raspberrywallet.manager.cryptography.common.Password;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.common.readers.WalletUUIDReader;
import org.apache.commons.lang.SerializationUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.UUID;

public class AuthorizationServerModule extends Module {
    
    // todo refactor it to take filename from conf, to avoid file collisions for multiple modules
    private final String encryptionPasswordFilename = "serverPassword.key";
    private Password encryptionPassword;
    
    private final WalletUUIDReader walletUUIDReader = WalletUUIDReader.getInstance();
    private final UUID walletUUID = walletUUIDReader.get();
    //todo change it to read data from conf yml
    private Credentials serverCredentials = new Credentials(walletUUID.toString(), "123");
    
    private final AuthorizationServerAPI serverAPI;
    
    //todo move this to correct method in Module interface
    public void initialize() {
        String rawPassword = new StringReader(encryptionPasswordFilename).get();
        encryptionPassword = new Password(rawPassword.toCharArray());
    }
    
    public AuthorizationServerModule() {
        serverAPI = new AuthorizationServerAPI(new AuthorizationServerConf(), serverCredentials);
        initialize();
    }
    
    public AuthorizationServerModule(String host, int port) {
        AuthorizationServerConf configuration = AuthorizationServerConf.builder().host(host).port(port).build();
        serverAPI = new AuthorizationServerAPI(configuration, serverCredentials);
        initialize();
    }
    
    public AuthorizationServerModule(AuthorizationServerConf configuration) {
        serverAPI = new AuthorizationServerAPI(configuration, serverCredentials);
        initialize();
    }
    
    @Override
    public String getDescription() {
        return "This module is authenticating user with external authorization server.";
    }
    
    @Override
    public boolean check() {
        return false;
    }
    
    @Override
    public byte[] encrypt(byte[] keyPart) throws EncryptionException {
        AESEncryptedObject<ByteWrapper> encryptedSecret =
                CryptoObject.encrypt(new ByteWrapper(keyPart), encryptionPassword);
        
        byte[] serializedSecret = SerializationUtils.serialize(encryptedSecret);
        String serializedAndEncodedSecret = Base64.getEncoder().encodeToString(serializedSecret);

        try {
            serverAPI.overwriteSecret(serializedAndEncodedSecret);
        } catch (RequestException e) {
            throw new EncryptionException("Failed to save encrypted key part on server.");
        }
    
        // since storing encrypted key part is external server logic
        // we just return empty byte array, because it's not important
        return new byte[1];
    }
    
    @Override
    public byte[] decrypt(byte[] payload) throws DecryptionException {
        try {
            String encodedSecret = serverAPI.getSecret();
            byte[] decodedSecret = Base64.getDecoder().decode(encodedSecret);
            
            AESEncryptedObject<ByteWrapper> deserializedEncryptedSecret =
                    (AESEncryptedObject<ByteWrapper>)SerializationUtils.deserialize(decodedSecret);
            
            return CryptoObject.decrypt(deserializedEncryptedSecret, encryptionPassword)
                    .getData();
            
        } catch (RequestException e) {
            throw new DecryptionException(e.getMessage());
        }
    }
    
    @Override
    public void register() {
    
    }
    
    @Nullable
    @Override
    public String getHtmlUi() {
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"text\" name=\"username\">");
        html.append("</br>");
        html.append("<input type=\"text\" name=\"password\">");
        return html.toString();
    }
    
}
