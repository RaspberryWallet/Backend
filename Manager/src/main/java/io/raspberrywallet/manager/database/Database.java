package io.raspberrywallet.manager.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Database {
	
	private List<WalletEntity> wallets = new ArrayList<WalletEntity>();
	
	public Database() throws IOException {
		this(false);
	}
	
	public void addWallets(List<WalletEntity> wallets) {
		this.wallets.addAll(wallets);
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
		for(WalletEntity we:wallets) {
			for(KeyPartEntity kpe: we.parts)
				kpe.clean();
			we.parts.clear();
		}
		wallets.clear();
	}
	
	/**
	 * Return all wallets serialized as JSON
	 * @return - JSON
	 */
	public byte[] getSerialized() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		byte[] data = mapper.writeValueAsBytes(wallets);
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

		wallets.addAll(deserialize(data));
	}

	/**
	 * JSON deserialization
	 * @param data - decrypted JSON data
	 * @return - wallet list
	 */
	public List<WalletEntity> deserialize(byte[] data) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		//TODO jak tego sie uzywa xdd
		List<WalletEntity> wallets = mapper.readValue(data, new TypeReference<List<WalletEntity>>(){});
		return wallets;
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
