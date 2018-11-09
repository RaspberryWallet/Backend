package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ToString
public class WalletEntity implements Serializable {
    
    @Getter
    @Setter
    @JsonProperty("keyparts")
    private List<KeyPartEntity> parts = new ArrayList<>();
    
    /**
     * Needed to override this, so `WalletEntity` can be easily compared.
     * Uses `List::containsAll` twice to be sure that the `List`s are 100% equal.
     * This method uses overridden `KeyPartEntity::equals` to determine membership.
     * Therefore different pointers will not negate the result.
     * `List::size` comparison is for optimization
     *
     * @return hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(parts);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WalletEntity) {
            WalletEntity otherWallet = (WalletEntity) obj;
            return (
                    this.parts.size() == otherWallet.parts.size()
                            && this.parts.containsAll(otherWallet.parts)
                            && otherWallet.parts.containsAll(this.parts)
            );
        }
        return super.equals(obj);
    }
}
