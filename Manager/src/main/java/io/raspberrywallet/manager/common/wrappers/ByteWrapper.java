package io.raspberrywallet.manager.common.wrappers;

import io.raspberrywallet.manager.common.ArrayDestroyer;
import io.raspberrywallet.manager.common.interfaces.Destroyable;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;

public class ByteWrapper implements Serializable, Destroyable {
    
    @Getter
    private byte[] data;
    
    public ByteWrapper(byte[] data) {
        this.data = data;
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        
        if (!(other instanceof ByteWrapper))
            return false;
        
        ByteWrapper otherCasted = (ByteWrapper)other;
        return this.hashCode() == otherCasted.hashCode();
    }
    
    @Override
    public void destroy() {
        ArrayDestroyer.destroy(data);
    }

}
