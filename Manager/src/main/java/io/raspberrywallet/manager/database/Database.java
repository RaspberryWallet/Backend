package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stasbar.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Database {

    @JsonProperty("wallet")
    public WalletEntity wallet = null;


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


    /**
     * zerofill everything in RAM
     */
    private synchronized void cleanUp() {
        if (wallet == null) return;
        for (KeyPartEntity kpe : wallet.getParts())
            kpe.clean();
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
        cleanUp();
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

    public byte[] encrypt(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(data);
        dos.flush();
        dos.close();
        //TODO encryption
        return baos.toByteArray();
    }

    public byte[] decrypt(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        InflaterInputStream iis = new InflaterInputStream(bais);
        int len = -1;
        byte[] buffer = new byte[4096];
        while ((len = iis.read(buffer)) != -1) ; //TODO join arrays into one blob
        //TODO decryption
        return buffer;
    }

    public void saveWallet(WalletEntity walletEntity) throws IOException {
        setWallet(walletEntity);
        saveDatabaseToFile();
    }

    public Optional<KeyPartEntity> getKeypartForModuleId(String id) {
        return wallet.getParts().stream().filter(keyPart -> keyPart.getModule().equals(id)).findFirst();
    }
}
