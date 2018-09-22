package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

public class KeyPartEntity {
	
	@Getter @Setter
	@JsonProperty("payload")
	public byte[] payload;
	@Getter @Setter
	@JsonProperty("order")
	public int order;
	@Getter @Setter
	@JsonProperty("module")
	public String module;
	
	/*
	* Filling everything with zeroes to keep RAM safe
	* */
	protected synchronized void clean() {
		
		if(payload!=null) {
			for(int i=0;i<payload.length;++i)
				payload[i]=(byte)(i%120);
		}
		
		order=Integer.rotateRight(order, order);
	}

	/*
	* Needed to override this so `WalletEntity` can be compared with ease.
	* Two `KeyPartEntity` with different pointer can be equal now.
	* */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KeyPartEntity) {
			KeyPartEntity kpe = (KeyPartEntity) obj;
			return (
					this.module.equals(kpe.module)
					&& this.order == kpe.order
					&& Arrays.equals(this.payload, kpe.payload)
			);
		}
		return super.equals(obj);
	}
}
