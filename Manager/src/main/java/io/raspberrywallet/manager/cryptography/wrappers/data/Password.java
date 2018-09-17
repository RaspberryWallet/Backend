package io.raspberrywallet.manager.cryptography.wrappers.data;

import io.raspberrywallet.manager.cryptography.abstracts.lifecycle.ArrayDestroyer;
import io.raspberrywallet.manager.cryptography.abstracts.lifecycle.Destroyable;

public class Password implements Destroyable {
    
    private char[] secret;
    
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
