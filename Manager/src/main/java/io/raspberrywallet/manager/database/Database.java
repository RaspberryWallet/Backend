package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.common.interfaces.Destroyable;
import io.raspberrywallet.manager.cryptography.common.Password;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;

public class Database implements Destroyable {

    private final static String DATABASE_FILE_NAME = "database.bin";

    @Getter
    @Setter
    @JsonProperty("wallet")
    private WalletEntity wallet = null;

    private final File databaseFile;
    private final Password password;

    public Database(Configuration config, Password password) throws IOException, DecryptionException, EncryptionException {
        this.password = password;
        databaseFile = new File(config.getBasePathPrefix(), DATABASE_FILE_NAME);
        loadDatabase();
    }

    @Override
    public synchronized void destroy() {
        if (wallet == null)
            return;

        wallet.getParts().forEach(KeyPartEntity::destroy);
        wallet.getParts().clear();
        wallet = null;
    }

    public void loadDatabase() throws IOException, DecryptionException, EncryptionException {
        if (!databaseFile.exists()) {
            databaseFile.getParentFile().mkdirs();
            databaseFile.createNewFile();
            saveDatabase();
        } else {
            loadDatabase(databaseFile);
        }
    }

    private void loadDatabase(File file) throws IOException, DecryptionException {
        if (Files.size(file.toPath()) == 0)
            return;

        destroy();
        byte[] encryptedDatabase = Files.readAllBytes(file.toPath());
        setWallet(decrypt(encryptedDatabase));
    }

    public byte[] encrypt() throws EncryptionException {
        return encrypt(wallet);
    }

    /**
     * Use encrypt() instead.
     * This method will become private.
     */
    @Deprecated
    public byte[] encrypt(WalletEntity wallet) throws EncryptionException {

        AESEncryptedObject<WalletEntity> encryptedData =
                CryptoObject.encrypt(wallet, password);

        return SerializationUtils.serialize(encryptedData);
    }

    /**
     * Use decrypt() instead.
     * This method will become private.
     */
    @Deprecated
    public WalletEntity decrypt(byte[] data) throws DecryptionException {
        AESEncryptedObject<WalletEntity> encryptedObject =
                (AESEncryptedObject<WalletEntity>) SerializationUtils.deserialize(data);

        return CryptoObject.decrypt(encryptedObject, password);
    }

    public void saveWallet() throws IOException, EncryptionException {
        saveWallet(wallet);
    }

    /**
     * Use saveEncryptedWallet() instead.
     */
    @Deprecated
    public void saveWallet(WalletEntity walletEntity) throws IOException, EncryptionException {
        setWallet(walletEntity);
        saveDatabase();
    }

    private void saveDatabase() throws IOException, EncryptionException {
        if (wallet == null)
            wallet = new WalletEntity();

        Files.write(databaseFile.toPath(), encrypt());
    }

    public Optional<KeyPartEntity> getKeypartForModuleId(String id) {
        return wallet.getParts().stream().filter(keyPart -> keyPart.getModule().equals(id)).findFirst();
    }

    public boolean addKeyPart(KeyPartEntity keyPartEntity) {
        return wallet.getParts().add(keyPartEntity);
    }

    public boolean addAllKeyParts(Collection<KeyPartEntity> keyPartEntities) {
        return wallet.getParts().addAll(keyPartEntities);
    }
}
