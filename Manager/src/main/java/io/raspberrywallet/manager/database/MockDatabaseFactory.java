package io.raspberrywallet.manager.database;

import java.io.IOException;
import java.util.ArrayList;

public class MockDatabaseFactory {

	/**
	 * Sztuczna baza danych do debugu
	 * @return nowa instancja
	 */
	public static MockDatabaseFactory getInstance() {
		return new MockDatabaseFactory();
	}
	
	private Database database;
	
	private ArrayList<WalletEntity> wallets = new ArrayList<WalletEntity>();
	private WalletEntity walletEntity;
	
	public MockDatabaseFactory() {
		try {
			database = new Database(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MockDatabaseFactory setBalance(double balance) {
		if(walletEntity == null) walletEntity=new WalletEntity();
		walletEntity.balance=balance;
		return this;
	}
	
	public MockDatabaseFactory setAddress(String address) {
		if(walletEntity == null) walletEntity = new WalletEntity();
		walletEntity.address = new String(address);
		return this;
	}
	
	public MockDatabaseFactory placeKeyPart(byte[] payload, int order) {
		KeyPartEntity kp = new KeyPartEntity();
		kp.order=order;
		kp.payload=payload.clone();
		if(walletEntity==null) walletEntity=new WalletEntity();
		walletEntity.parts.add(kp);
		return this;
	}
	
	public MockDatabaseFactory pushWallet() {
		wallets.add(walletEntity);
		walletEntity = null;
		return this;
	}
	
	public Database getDatabase() {
		System.out.println("Wallets: "+wallets.size());
		database.addWallets(wallets);
		wallets.clear();
		return database;
	}
	
}
