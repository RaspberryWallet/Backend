package io.raspberrywallet.manager.cryptography.common;

import io.raspberrywallet.manager.common.ArrayDestroyer;
import io.raspberrywallet.manager.common.interfaces.Destroyable;

public class Password implements Destroyable {
    
    private char[] secret;
    
    /**
     * Since in most of cases, secret is a String, that is
     * converted to char array, then adding this constructor
     * makes sense. Anyways, secrets shouldn't be stored as
     * String, due to problems with removing it from memory.
     * Char and byte array can be manually overwritten.
     * @param secret
     */
    public Password(String secret) {
        this.secret = secret.toCharArray();
    }
    
    public Password(char[] secret) {
        this.secret = secret;
    }
    
    public Password(byte[] secret) {
        String string = new String(secret);
        this.secret = string.toCharArray();
    }
    
    public char[] getSecret() {
        return secret;
    }
    
    public void destroy() {
        ArrayDestroyer.destroy(secret);
    }
    
}
