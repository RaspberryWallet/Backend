package io.raspberrywallet.manager.modules.authorizationserver;

import java.io.IOException;

class RequestException extends Exception {
    
    RequestException(String msg) {
        super(msg);
    }
    
    RequestException(IOException e) {
        super(e);
    }

}