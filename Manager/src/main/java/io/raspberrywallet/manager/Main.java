package io.raspberrywallet.manager;

import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.server.Server;

public class Main {

    public static void main(String... args) {
        Bitcoin bitcoin = new Bitcoin();
        bitcoin.startBlockchainAsync();

        TemperatureMonitor temperatureMonitor = new TemperatureMonitor();

        Manager manager = new ExampleMockManager(bitcoin, temperatureMonitor);

        Server server = new Server(manager);
        server.start();
    }
}
