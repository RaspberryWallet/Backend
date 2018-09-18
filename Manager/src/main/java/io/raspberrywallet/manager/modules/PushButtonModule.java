package io.raspberrywallet.manager.modules;

import com.pi4j.io.gpio.*;

import io.raspberrywallet.manager.modules.Module.DecryptionException;

public class PushButtonModule extends Module {

	final GpioController gpio = GpioFactory.getInstance();
	final GpioPinDigitalInput pushButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23);

	@Override
	public String getDescription() {
		return "Module for pushing physical button on the hardware wallet.";
	}

	@Override
	public boolean check() {
		if(pushButton.isHigh()) return true;
		return false;
	}

	@Override
	public void process() {
		decrypt(new Decrypter() {
			@Override
			public byte[] decrypt(byte[] payload) throws DecryptionException {
				//TODO dekrypt desem i te sprawy
				return payload;
			}
		});

	}

	@Override
	public void register() {
		// TODO chyba nic
			
	}

	@Override
	public byte[] encryptInput(byte[] data, Object... params) {
		// TODO też enkrypcja i inne takie tam
		return data;
	}
	
}
