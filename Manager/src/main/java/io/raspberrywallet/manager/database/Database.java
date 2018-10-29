package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stasbar.Logger;
import io.raspberrywallet.manager.common.interfaces.Destroyable;
import io.raspberrywallet.manager.common.wrappers.ByteWrapper;
import io.raspberrywallet.manager.cryptography.common.Password;
import io.raspberrywallet.manager.cryptography.crypto.AESEncryptedObject;
import io.raspberrywallet.manager.cryptography.crypto.CryptoObject;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Database implements Destroyable {

    @JsonProperty("wallet")
    public WalletEntity wallet = null;

    //TODO change this to accept user input as password
    private Password password = new Password("abcdag".toCharArray());

    private File databaseFile;

    public Database() throws IOException {
        this(false);
    }


    public Database(boolean mock) throws IOException {

        if (mock) {
            return;
        }

        databaseFile = new File("./database.bin");
        if (!databaseFile.exists()) {
            databaseFile.getParentFile().mkdirs();
            databaseFile.createNewFile();
            saveDatabase(databaseFile);
        } else {
            loadDatabase(databaseFile);
        }
    }

    public void setWallet(WalletEntity wallet) {
        this.wallet = wallet;
    }

    
    @Override
    public synchronized void destroy() {
        if (wallet == null) return;
        for (KeyPartEntity kpe : wallet.getParts())
            kpe.destroy();
        wallet.getParts().clear();
        wallet = null;
    }

    /**
     * Return all wallets serialized as JSON
     *
     * @return - JSON
     */
    public byte[] getSerialized() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        byte[] data = mapper.writeValueAsBytes(wallet);
        System.out.println(new String(data));
        return data;
    }

    /**
     * Loading database from encrypted file
     *
     * @param file - encrypted JSON file
     * @throws IOException - filesystem problem
     */
    private void loadDatabase(File file) throws IOException {
        if (Files.size(file.toPath()) == 0) return;
        destroy();
        byte[] data = Files.readAllBytes(file.toPath());
        //todo enable decrypting
        //data = decrypt(data);

        setWallet(deserialize(data));
    }

    /**
     * JSON deserialization
     *
     * @param data - decrypted JSON data
     * @return - wallet list
     */
    public WalletEntity deserialize(byte[] data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //TODO jak tego sie uzywa xdd
        Logger.d("deserialize: " + new String(data));
        WalletEntity wallet = mapper.readValue(data, new TypeReference<WalletEntity>() {
        });

        return wallet;
    }

    /**
     * Save encrypted database to file
     *
     * @param file - destination file
     * @throws IOException - filesystem problem
     */
    private void saveDatabase(File file) throws IOException {
        byte[] data = getSerialized();
        //todo enable encrypting
        //data = encrypt(data);
        Files.write(file.toPath(), data);
    }

    private void saveDatabaseToFile() throws IOException {
        saveDatabase(databaseFile);
    }

    public byte[] encrypt(byte[] data) throws EncryptionException {
        ByteWrapper wrappedData = new ByteWrapper(data);
        
        AESEncryptedObject<ByteWrapper> encryptedData =
                CryptoObject.encrypt(wrappedData, password);
        
        return SerializationUtils.serialize(encryptedData);
    }

    public byte[] decrypt(byte[] data) throws DecryptionException {
        AESEncryptedObject<ByteWrapper> encryptedObject =
                (AESEncryptedObject<ByteWrapper>) SerializationUtils.deserialize(data);
        
        return CryptoObject.decrypt(encryptedObject, password).getData();
    }

    public void saveWallet(WalletEntity walletEntity) throws IOException {
        setWallet(walletEntity);
        saveDatabaseToFile();
    }

    public Optional<KeyPartEntity> getKeypartForModuleId(String id) {
        return wallet.getParts().stream().filter(keyPart -> keyPart.getModule().equals(id)).findFirst();
    }
}
