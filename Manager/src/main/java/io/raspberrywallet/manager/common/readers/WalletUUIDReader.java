package io.raspberrywallet.manager.common.readers;

import lombok.Synchronized;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class WalletUUIDReader extends Reader<UUID> {
    
    private final static String UUID_FILE_NAME = "wallet.uuid";
    private final static WalletUUIDReader INSTANCE = new WalletUUIDReader();
    
    private UUID walletUUID;
    
    private WalletUUIDReader() {}
    
    public static WalletUUIDReader getInstance() {
        return INSTANCE;
    }
    
    @Synchronized
    public UUID get() {
        if (walletUUID != null)
            return walletUUID;
        
        if (fileExists()) {
            walletUUID = read();
            return walletUUID;
        }
    
        walletUUID = UUID.randomUUID();
        write(walletUUID);
        return walletUUID;
    }
    
    @Override
    String getFilePath() {
        return WALLET_DIR + UUID_FILE_NAME;
    }
    
}
