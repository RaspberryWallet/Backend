package io.raspberrywallet.manager.common.generators;

import java.nio.charset.Charset;
import java.security.SecureRandom;

public abstract class RandomStringGenerator {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private final static char UNICODE_REPLACEMENT_CHARACTER = 65533;
    
    private RandomStringGenerator() {}
    
    public static String get(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        abs(randomBytes);
        
        String string = new String(randomBytes, Charset.forName("UTF-8"));
        if (string.contains(String.valueOf(UNICODE_REPLACEMENT_CHARACTER)))
            throw new IllegalStateException("There was problem with string generation. It contains Unicode replacement characters.");
        return string;
    }
    
    private static void abs(byte[] input) {
        for (int i = 0; i < input.length; i++)
            input[i] = abs(input[i]);
    }
    
    /**
     * Due to problems with UTF transforming negative numbers, 0 and 128 into 65533
     * This method converts them into correct UTF values.
     */
    private static byte abs(byte input) {
        if (input == 0) {
            byte[] newInput = new byte[1];
            newInput[0] = 0;
            while (newInput[0] == 0)
                secureRandom.nextBytes(newInput);
    
            // newInput is non-zero, now let's get an abs value
            return abs(newInput[0]);
        }
        // if input is -128, then with abs it would be 128, which is not
        // supported value for UTF and is converted to 65533
        // because of this, we are going to reroll this input
        if (input < -127) {
            input = 0;
            abs(input);
        }
        return input < 0 ? (byte)( -input) : input;
    }
    
}
