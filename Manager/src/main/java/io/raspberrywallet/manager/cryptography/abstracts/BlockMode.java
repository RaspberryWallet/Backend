package io.raspberrywallet.manager.cryptography.abstracts;

import java.util.function.Supplier;

public enum BlockMode implements Supplier<String> {
    ECB("ECB", false),
    CBC("CBC", true),
    CFB("CFB", true),
    OFB("OFB", true);
    
    public String fullName;
    public boolean initVectorRequired;
    
    BlockMode(String fullName, boolean initVectorRequired) {
        this.fullName = fullName;
        this.initVectorRequired = initVectorRequired;
    }
    
    
    @Override
    public String get() {
        return fullName;
    }
}