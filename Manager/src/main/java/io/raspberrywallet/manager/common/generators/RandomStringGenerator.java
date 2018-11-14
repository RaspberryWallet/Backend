package io.raspberrywallet.manager.common.generators;

import java.util.Base64;

import java.security.SecureRandom;

public abstract class RandomStringGenerator {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private final static char UNICODE_REPLACEMENT_CHARACTER = 65533;
    
    private RandomStringGenerator() {}
    
    public static String get(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        String encodedString = Base64.getEncoder().encodeToString(randomBytes);
        if (encodedString.contains(String.valueOf(UNICODE_REPLACEMENT_CHARACTER)))
            throw new IllegalStateException("There was problem with string generation. It contains Unicode replacement characters.");
        
        return encodedString;
    }
    
}
