package io.raspberrywallet.manager;

import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.server.Server;

import static io.raspberrywallet.ktor.KtorServerKt.startKtorServer;

public class Main {

    public static void main(String... args) {
        Bitcoin bitcoin = new Bitcoin();

        // Disable for now
        //Service blockchainSyncing = bitcoin.startBlockchainAsync();
        //Runtime.getRuntime().addShutdownHook(new Thread(blockchainSyncing::stopAsync));

        TemperatureMonitor temperatureMonitor = new TemperatureMonitor();

        Manager manager = new ExampleMockManager(bitcoin, temperatureMonitor);

        if (args.length > 0 && args[0].equals("ktor"))
            startKtorServer(manager);
        else
            new Server(manager).start();

    }
}
