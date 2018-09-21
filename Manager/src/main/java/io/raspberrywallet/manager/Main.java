package io.raspberrywallet.manager;

import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.server.Server;

public class Main {

    public static void main(String... args) {
        Bitcoin bitcoin = new Bitcoin();
        bitcoin.startBlockchainAsync();

        Manager manager = new ExampleMockManager(bitcoin);

        Server server = new Server(manager);
        server.start();
    }
}
