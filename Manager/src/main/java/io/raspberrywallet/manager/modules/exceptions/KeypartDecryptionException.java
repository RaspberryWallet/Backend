package io.raspberrywallet.manager.modules.exceptions;

public class KeypartDecryptionException extends Throwable {

    public static final int NO_DATA = -1;
    public static final int BAD_KEY = -2;
    public static final int UNKNOWN = -3;

    private int code;

    public KeypartDecryptionException(int code) {
        this.code = code;
    }

    int getCode() {
        return this.code;
    }

    public String getMessage() {
        switch (code) {
            case NO_DATA:
                return "-1: No paylaod data specified.";
            case BAD_KEY:
                return "-2: Wrong key provided for decryption.";
            case UNKNOWN:
                return "-3: Unknown error.";
        }
        return "??: This is an error LOL.";
    }

}
