package io.raspberrywallet.manager.cryptography.exceptions;

public class DecryptionException extends Exception {
    
    public DecryptionException(String message) {
        super(message);
    }
    
    public DecryptionException(Throwable throwable) {
        super(throwable);
    }
}
