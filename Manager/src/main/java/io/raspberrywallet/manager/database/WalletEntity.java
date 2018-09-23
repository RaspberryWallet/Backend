package io.raspberrywallet.manager.database;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class WalletEntity {
	
	@Getter @Setter
	@JsonProperty("keyparts")
	public List<KeyPartEntity> parts = new ArrayList<KeyPartEntity>();
	@Getter @Setter
	@JsonProperty("address")
	public String address="000";
	@Getter @Setter
	@JsonProperty("balance")
	public double balance=0.0;

	/*
	* Needed to override this, so `WalletEntity` can be easily compared.
	* Uses `List::containsAll` twice to be sure that the `List`s are 100% equal.
	* This method uses overridden `KeyPartEntity::equals` to determine membership.
	* Therefore different pointers will not negate the result.
	* `List::size` comparison is for optimization.
	* */

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof WalletEntity) {
			WalletEntity we = (WalletEntity) obj;
			return(
					this.parts.size() == we.parts.size()
					&& this.parts.containsAll(we.parts)
					&& we.parts.containsAll(this.parts)
					&& this.balance == we.balance
					&& this.address.equals(we.address)
			);
		}
		return super.equals(obj);
	}
}
