package io.raspberrywallet.manager.common.wrappers;

import io.raspberrywallet.manager.common.interfaces.Destroyable;

import java.util.Base64;

public class Secret implements Destroyable {
    
    private ByteWrapper byteWrapper;
    
    public byte[] getData() {
        return byteWrapper.getData();
    }
    
    public Secret(byte[] data) {
        byte[] dataConverted = Base64.getEncoder().encode(data);
        byteWrapper = new ByteWrapper(dataConverted);
    }
    
    public byte[] decode() {
        return Base64.getDecoder().decode(byteWrapper.getData());
    }
    
    @Override
    public void destroy() {
        byteWrapper.destroy();
    }
}
