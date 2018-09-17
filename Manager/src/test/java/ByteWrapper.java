import java.io.Serializable;
import java.util.Arrays;

public class ByteWrapper implements Serializable {
    
    private byte[] data;
    
    public ByteWrapper(byte[] data) {
        this.data = data;
    }
    
    public byte[] getData() {
        return data;
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
}
