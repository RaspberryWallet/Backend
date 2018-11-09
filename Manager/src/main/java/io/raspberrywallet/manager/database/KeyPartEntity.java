package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.raspberrywallet.manager.common.interfaces.Destroyable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Arrays;

@ToString
@NoArgsConstructor
public class KeyPartEntity implements Destroyable, Serializable {
  
    @Getter
    @Setter
    @JsonProperty("payload")
    private byte[] payload;

    @Getter
    @Setter
    @JsonProperty("module")
    private String module;

    public KeyPartEntity(byte[] payload, String module) {
        this.payload = payload;
        this.module = module;
    }
    
    @Override
    public synchronized void destroy() {
        
        if (payload != null) {
            for (int i = 0; i < payload.length; ++i)
                payload[i] = (byte) (i % 120);
        }
    }
    
    /**
     * Needed to override this so `WalletEntity` can be compared with ease.
     * Two `KeyPartEntity` with different pointer can be equal now.
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyPartEntity) {
            KeyPartEntity kpe = (KeyPartEntity) obj;
            return (
                    this.module.equals(kpe.module)
                            && Arrays.equals(this.payload, kpe.payload)
            );
        }
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + module.hashCode();
        result = 31 * result + Arrays.hashCode(payload);
        return result;
    }
    
}
