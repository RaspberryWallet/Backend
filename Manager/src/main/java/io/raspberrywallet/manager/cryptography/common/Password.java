package io.raspberrywallet.manager.cryptography.common;

import io.raspberrywallet.manager.common.ArrayDestroyer;
import io.raspberrywallet.manager.common.interfaces.Destroyable;

public class Password implements Destroyable {
    
    private String secret;
    
    /**
     * Since in most of cases, secret is a String, that is
     * converted to char array, then adding this constructor
     * makes sense. Anyways, secrets shouldn't be stored as
     * String, due to problems with removing it from memory.
     * Char and byte array can be manually overwritten.
     * @param secret
     */
    public Password(String secret) {
        this.secret = secret;
    }
    
    public Password(byte[] secret) {
        this.secret = new String(secret);
    }
    
    public String getSecret() {
        return secret;
    }
    
    public void destroy() {
        secret = null;
    }
    
}
