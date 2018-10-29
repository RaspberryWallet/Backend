package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.raspberrywallet.manager.common.interfaces.Destroyable;
import io.raspberrywallet.manager.cryptography.common.Password;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.apache.commons.lang.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class Database implements Destroyable {
    
    //TODO change this to load from configuration
    private final static String DATABASE_FILE_PATH = "./database.bin";
    
    @JsonProperty("wallet")
    public WalletEntity wallet = null;
    
    //TODO change this to accept user input as password
    private Password password = new Password("changeme".toCharArray());

    private File databaseFile;

    public Database() throws IOException, DecryptionException, EncryptionException {
        this(false);
    }

    public Database(boolean mock) throws IOException, DecryptionException, EncryptionException {
        if (mock)
            return;

        databaseFile = new File(DATABASE_FILE_PATH);
        if (!databaseFile.exists()) {
            databaseFile.getParentFile().mkdirs();
            databaseFile.createNewFile();
            saveDatabase();
        } else {
            loadDatabase(databaseFile);
        }
    }

    public void setWallet(WalletEntity wallet) {
        this.wallet = wallet;
    }
    
    @Override
    public synchronized void destroy() {
        if (wallet == null)
            return;
        
        wallet.getParts().forEach(KeyPartEntity::destroy);
        wallet.getParts().clear();
        wallet = null;
    }

    /**
     * Return all wallets serialized as JSON
     * @return - JSON
     */
    public byte[] getSerialized() {
        return SerializationUtils.serialize(wallet);
    }

    /**
     * Loading database from encrypted file
     * @param file - encrypted JSON file
     * @throws IOException - filesystem problem
     */
    private void loadDatabase(File file) throws IOException, DecryptionException {
        if (Files.size(file.toPath()) == 0)
            return;
        
        destroy();
        byte[] encryptedDatabase = Files.readAllBytes(file.toPath());
        setWallet(decrypt(encryptedDatabase));
    }

    /**
     * Save encrypted database to file
     * @throws IOException - filesystem problem
     */
    private void saveDatabase() throws IOException, EncryptionException {
        Files.write(databaseFile.toPath(), encrypt(wallet));
    }

    public byte[] encrypt(WalletEntity wallet) throws EncryptionException {
        
        AESEncryptedObject<WalletEntity> encryptedData =
                CryptoObject.encrypt(wallet, password);
        
        return SerializationUtils.serialize(encryptedData);
    }

    public WalletEntity decrypt(byte[] data) throws DecryptionException {
        AESEncryptedObject<WalletEntity> encryptedObject =
                (AESEncryptedObject<WalletEntity>) SerializationUtils.deserialize(data);
        
        return CryptoObject.decrypt(encryptedObject, password);
    }

    public void saveWallet(WalletEntity walletEntity) throws IOException, EncryptionException {
        setWallet(walletEntity);
        saveDatabase();
    }

    public Optional<KeyPartEntity> getKeypartForModuleId(String id) {
        return wallet.getParts().stream().filter(keyPart -> keyPart.getModule().equals(id)).findFirst();
    }
}
