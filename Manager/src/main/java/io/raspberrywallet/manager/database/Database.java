package io.raspberrywallet.manager.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	 * "zerujemy" wszystko co niebezpieczne
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
	 * Zwróć wszystkie wallety zserializowane jako JSON
	 * @return - JSON
	 */
	public byte[] getSerialized() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		byte[] data = mapper.writeValueAsBytes(wallets);
		return data;
	}
	
	/**
	 * Wczytujemy bazę danych z zaszyfrowanego pliku
	 * @param file - zaszyfrowany JSON
	 * @throws IOException - problem plików
	 */
	private void loadDatabase(File file) throws IOException {
		cleanUp();
		byte[] data = Files.readAllBytes(file.toPath());
		data = decrypt(data);

		wallets.addAll(deserialize(data));
	}

	/**
	 * Deserializacja z JSONa
	 * @param data - rozszyfrowane dane z JSONa
	 * @return - lista portfeli
	 */
	public List<WalletEntity> deserialize(byte[] data) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		//TODO jak tego sie uzywa xdd
		List<WalletEntity> wallets = mapper.readValue(data, List.class);
		return wallets;
	}

	/**
	 * Zapisujemy zaszyfrowaną bazę danych do pliku
	 * @param file - plik z bazą
	 * @throws IOException - problem plików
	 */
	private void saveDatabase(File file) throws IOException {
		byte[] data=encrypt(getSerialized());
		Files.write(file.toPath(), data);
	}
	
	private byte[] encrypt(byte[] data) {
		//TODO enkrypcja
		return data;
	}
	
	private byte[] decrypt(byte[] data) {
		//TODO dekrypcja
		return data;
	}
	
}
