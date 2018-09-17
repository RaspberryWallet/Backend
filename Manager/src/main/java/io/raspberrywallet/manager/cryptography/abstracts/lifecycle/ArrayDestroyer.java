package io.raspberrywallet.manager.cryptography.abstracts.lifecycle;

import java.util.Arrays;

public abstract class ArrayDestroyer {
    
    private ArrayDestroyer() {
    
    }
    
    public static void destroy(byte[] array) {
        Arrays.fill(array, (byte)0);
    }
    
    public static void destroy(char[] array) {
        Arrays.fill(array, (char)0);
    }
    
    public static <E> void destroy(E[] array) {
        Arrays.fill(array, null);
    }
    
}
