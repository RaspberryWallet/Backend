package io.raspberrywallet.manager.common.readers;

import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

abstract class Reader <T extends Serializable> {
    
    final static String WALLET_DIR = "/opt/wallet/";
    
    private Path pathToFile;
    
    abstract String getFilePath();
    
    T read() {
        try {
            return (T)SerializationUtils.deserialize(Files.readAllBytes(pathToFile));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    void write(T input) {
        if (pathToFile == null)
            readFilePath();
        
        try {
            byte[] serializedData = SerializationUtils.serialize(input);
            Files.write(pathToFile, serializedData,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    boolean fileExists() {
        if (pathToFile == null)
            readFilePath();
        
        return Files.exists(pathToFile);
    }
    
    private void readFilePath() {
        pathToFile = Paths.get(getFilePath());
    }
}
