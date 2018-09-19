package io.raspberrywallet.manager;

import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.server.Server;
import org.bitcoinj.kits.WalletAppKit;

public class Main {

    public static void main(String... args) {
        Bitcoin bitcoin = new Bitcoin();
        WalletAppKit kit = bitcoin.startBlockchainAsync();

        Manager manager = new ExampleMockManager(bitcoin);

        Server server = new Server(manager);
        server.start();
    }
}
