package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.util.zip.*;
import java.io.*;

public class Database {

    @Getter
    @JsonProperty("wallet")
    public WalletEntity wallet = null;

    public Database() throws IOException {
        this(false);
    }

    public void setWallet(WalletEntity wallet) {
        this.wallet = wallet;
    }

    public Database(boolean mock) throws IOException {

        if (mock) {
            return;
        }

        File databaseFile = new File("/var/wallet/database.bin");
        if (!databaseFile.exists()) {
            databaseFile.createNewFile();
            saveDatabase(databaseFile);
        } else {
            loadDatabase(databaseFile);
        }
    }

    /**
     * zerofill everything in RAM
     */
    private synchronized void cleanUp() {
        for (KeyPartEntity kpe : wallet.parts)
            kpe.clean();
        wallet.parts.clear();
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
        cleanUp();
        byte[] data = Files.readAllBytes(file.toPath());
        data = decrypt(data);

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
        byte[] data = encrypt(getSerialized());
        Files.write(file.toPath(), data);
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
        while ( (len = iis.read(buffer)) != -1); //TODO join arrays into one blob
    	//TODO decryption
        return buffer;
    }

}
