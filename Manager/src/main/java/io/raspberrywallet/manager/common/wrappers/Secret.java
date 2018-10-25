package io.raspberrywallet.manager.common.wrappers;

import io.raspberrywallet.manager.common.interfaces.Destroyable;

import java.util.Base64;

public class Secret implements Destroyable {
    
    ByteWrapper byteWrapper;
    
    public byte[] getData() {
        return byteWrapper.getData();
    }
    
    Secret() {}
    
    /**
     * Warning, this constructor assumes, that given data is encoded in Base64
     * @param base64Data base64 encoded data
     */
    public Secret(String base64Data) {
        byte[] decodedData = Base64.getDecoder().decode(base64Data);
        byteWrapper = new ByteWrapper(decodedData);
    }
    
    @Override
    public void destroy() {
        byteWrapper.destroy();
    }
    
    @Override
    public int hashCode() {
       return byteWrapper.hashCode();
    }
}
