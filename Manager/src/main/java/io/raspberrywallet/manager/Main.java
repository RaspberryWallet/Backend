package io.raspberrywallet.manager;

import io.raspberrywallet.server.Server;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;

import java.io.File;

public class Main {

    public static void main(String... args) {
        WalletAppKit kit = startBlockchainSync(args);

        Manager manager = new ExampleMockManager(kit);

        Server server = new Server(manager);
        server.start();
    }

    private static WalletAppKit startBlockchainSync(String... args) {
        BriefLogFormatter.init();
        // Figure out which network we should connect to. Each one gets its own set of files.
        NetworkParameters params;
        String filePrefix;
        if (args.length >= 1 && args[0].equals("mainnet")) {
            params = MainNetParams.get();
            filePrefix = "wallet";
        } else {
            params = TestNet3Params.get();
            filePrefix = "wallet-testnet";
        }

        // Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
        WalletAppKit kit = new WalletAppKit(params, new File("."), filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
            }
        };
        // Download the block chain
        kit.startAsync();

        return kit;
    }
}
