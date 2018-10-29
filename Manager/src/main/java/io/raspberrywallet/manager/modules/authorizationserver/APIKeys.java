package io.raspberrywallet.manager.modules.authorizationserver;

enum APIKeys {
    
    WALLETUUID("walletUUID"),
    PASSWORD("password"),
    SECRET("secret"),
    TOKEN("token"),
    SESSION_LENGTH("sessionLength");
    
    String val;
    
    APIKeys(String val) {
        this.val = val;
    }
    
    @Override
    public String toString() {
        return val;
    }
    
}
