package io.raspberrywallet.manager.common.readers;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;

import java.security.SecureRandom;

public class StringReader extends Reader<String> {
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Getter
    @Setter
    private int stringSizeInBytes = 16;
    
    private final String filePath;
    
    private String value;
    
    public StringReader(String filePath) {
        this.filePath = WALLET_DIR + filePath;
    }
    
    /**
     * Get String from file, or if it does not exists,
     * then get random String and save it to file.
     * @return String read from file or randomly generated with SecureRandom.
     */
    @Synchronized
    public String get() {
        if (value != null)
            return value;
        
        if (fileExists()) {
            value = read();
            return value;
        }
        
        value = getRandomString();
        write(value);
        return value;
    }
    
    private String getRandomString() {
        byte[] randomBytes = new byte[stringSizeInBytes];
        secureRandom.nextBytes(randomBytes);
        return new String(randomBytes);
    }
    
    @Override
    String getFilePath() {
        return filePath;
    }
    
}
