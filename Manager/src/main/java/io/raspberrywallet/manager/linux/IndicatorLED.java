package io.raspberrywallet.manager.linux;

import com.pi4j.io.gpio.*;
import lombok.Setter;

public class IndicatorLED {

    private final Pin ledPin = RaspiPin.GPIO_05;
    private final GpioController gpioController = GpioFactory.getInstance();
    private GpioPinDigitalOutput ledOutput;

    public IndicatorLED() {
        ledOutput = gpioController.provisionDigitalOutputPin(ledPin);
    }

    private Thread blinkThread;
    private BlinkRunnable blinkRunnable;

    @Setter
    private class BlinkRunnable implements Runnable {

        private int offMs = 50;
        private int onMs = 50;
        private boolean work = true;

        public BlinkRunnable(int offMs, int onMs) {
            setOffMs(offMs);
            setOnMs(onMs);
            setWork(true);
        }

        @Override
        public void run() {
            while (work) {

                ledOutput.blink(offMs, onMs);

                try {
                    Thread.sleep(offMs + onMs);
                } catch (Exception ignored) { }

            }
        }

    }

    /**
     * Set the indicator LED (GPIO_24) to ON.
     */
    public synchronized void on() {
        stopLed();
        ledOutput.high();
    }

    /**
     * Set the indicator LED (GPIO_24) to OFF.
     */
    public synchronized void off() {
        stopLed();
    }

    /**
     * Starts blinking until off()/on() is called.
     *
     * @param offMs - how long should the led be dark in miliseconds
     * @param onMs  - how long should the led be bright in miliseconds
     */
    public synchronized void blink(int offMs, int onMs) {

        if (blinkThread != null) {
            stopLed();
        }

        blinkRunnable = new BlinkRunnable(offMs, onMs);
        blinkThread = new Thread(blinkRunnable);
        blinkThread.start();

    }

    private synchronized void stopLed() {

        //Cancel blinking thread if it exists
        if (blinkThread != null) {

            blinkRunnable.setWork(false);
            try {
                blinkThread.join();
            } catch (Exception ignored) { }
            blinkThread = null;
        }

        ledOutput.low();
    }

}
