package io.raspberrywallet.manager.common.wrappers;

import java.util.Base64;

public class Base64Secret extends Secret {
    
    public Base64Secret(byte[] data) {
        byte[] dataConverted = Base64.getEncoder().encode(data);
        byteWrapper = new ByteWrapper(dataConverted);
    }
    
}
