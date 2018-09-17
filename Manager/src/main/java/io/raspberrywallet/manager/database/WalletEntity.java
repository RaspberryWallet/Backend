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
	
}
