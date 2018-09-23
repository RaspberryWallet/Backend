package io.raspberrywallet.manager.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

public class Database {

	@Getter
	@JsonProperty("wallet")
	public WalletEntity wallet = null;
	
	public Database() throws IOException {
		this(false);
	}
	
	public void setWallet(WalletEntity wallets) {
		this.wallet = wallet;
	}
	
	public Database(boolean mock) throws IOException {
		
		if(mock) {
			return;
		}
		
		File databaseFile = new File("/var/wallet/database.bin");
		if(!databaseFile.exists()) {
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
			for(KeyPartEntity kpe: wallet.parts)
				kpe.clean();
			wallet.parts.clear();
		wallet = null;
	}
	
	/**
	 * Return all wallets serialized as JSON
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
	 * @param data - decrypted JSON data
	 * @return - wallet list
	 */
	public WalletEntity deserialize(byte[] data) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		//TODO jak tego sie uzywa xdd
		WalletEntity wallet = mapper.readValue(data, new TypeReference<WalletEntity>(){});
		return wallet;
	}

	/**
	 * Save encrypted database to file
	 * @param file - destination file
	 * @throws IOException - filesystem problem
	 */
	private void saveDatabase(File file) throws IOException {
		byte[] data=encrypt(getSerialized());
		Files.write(file.toPath(), data);
	}
	
	public byte[] encrypt(byte[] data) {
		//TODO encryption
		return data;
	}
	
	public byte[] decrypt(byte[] data) {
		//TODO decryption
		return data;
	}
	
}
