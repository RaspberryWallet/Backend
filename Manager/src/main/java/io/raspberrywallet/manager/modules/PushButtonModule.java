package io.raspberrywallet.manager.modules;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class PushButtonModule extends Module {

    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalInput pushButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_23);

    @Override
    public String getDescription() {
        return "Module for pushing physical button on the hardware wallet.";
    }

    @Override
    public boolean check() {
        return hasInput("pressed") && Boolean.parseBoolean(getInput("pressed"));
    }

    @Override
    public void register() {
        pushButton.addListener((GpioPinListenerDigital) event ->
                setInput("pressed", event.getState().isHigh() + "")
        );
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
