package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WalletEntity {
	
	@Getter @Setter
	@JsonProperty("keyparts")
	public List<KeyPartEntity> parts = new ArrayList<KeyPartEntity>();

	/*
	* Needed to override this, so `WalletEntity` can be easily compared.
	* Uses `List::containsAll` twice to be sure that the `List`s are 100% equal.
	* This method uses overridden `KeyPartEntity::equals` to determine membership.
	* Therefore different pointers will not negate the result.
	* `List::size` comparison is for optimization.
	* */

    @Override
    public int hashCode() {
        return Objects.hash(parts);
    }

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof WalletEntity) {
			WalletEntity we = (WalletEntity) obj;
			return(
					this.parts.size() == we.parts.size()
					&& this.parts.containsAll(we.parts)
					&& we.parts.containsAll(this.parts)
			);
		}
		return super.equals(obj);
	}
}
