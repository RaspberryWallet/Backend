package io.raspberrywallet.manager.database;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Fake database for debug purposes
 */
public class MockDatabaseFactory {

    public static MockDatabaseFactory getInstance() {
        return new MockDatabaseFactory();
    }

    private Database database;
    WalletEntity wallet = null;
    private WalletEntity walletEntity;

    public MockDatabaseFactory() {
        try {
            database = new Database(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MockDatabaseFactory setBalance(double balance) {
        if (walletEntity == null) walletEntity = new WalletEntity();
        walletEntity.balance = balance;
        return this;
    }

    public MockDatabaseFactory setAddress(String address) {
        if (walletEntity == null) walletEntity = new WalletEntity();
        walletEntity.address = new String(address);
        return this;
    }

    public MockDatabaseFactory placeKeyPart(byte[] payload, int order) {
        KeyPartEntity kp = new KeyPartEntity();
        kp.order = order;
        kp.payload = payload.clone();
        if (walletEntity == null) walletEntity = new WalletEntity();
        walletEntity.parts.add(kp);
        return this;
    }

    public MockDatabaseFactory pushWallet() {
        wallet = walletEntity;
        walletEntity = null;
        return this;
    }

    public Database getDatabase() {
        database.setWallet(wallet);
        return database;
    }

}
