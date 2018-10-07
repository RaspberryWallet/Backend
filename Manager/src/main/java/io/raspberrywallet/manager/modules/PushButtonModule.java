package io.raspberrywallet.manager.modules;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;

public class PushButtonModule extends Module {

    final GpioController gpio = GpioFactory.getInstance();
    final GpioPinDigitalInput pushButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23);

    @Override
    public String getDescription() {
        return "Module for pushing physical button on the hardware wallet.";
    }

    @Override
    public boolean check() {
        if (pushButton.isHigh()) return true;
        return false;
    }

    @Override
    public void register() {
    }

    @Override
    public String getHtmlUi() {
        return null;
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] decrypt(byte[] payload) {
        return payload;
    }
}
