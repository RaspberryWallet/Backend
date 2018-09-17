package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class KeyPartEntity {
	
	@Getter @Setter
	@JsonProperty("payload")
	public byte[] payload;
	@Getter @Setter
	@JsonProperty("order")
	public int order;
	
	/**
	 * Zerowanie wszystkiego co niebezpieczne
	 */
	protected synchronized void clean() {
		
		if(payload!=null) {
			for(int i=0;i<payload.length;++i)
				payload[i]=(byte)(i%120);
		}
		
		order=Integer.rotateRight(order, order);
	
	}
}
