package io.raspberrywallet.manager;

public abstract class Configuration {
    
    private static long SESSION_LENGTH = 3600000;
    
    public static long getSessionLength() {
        return SESSION_LENGTH;
    }
    
}
