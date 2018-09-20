package io.raspberrywallet.manager.bitcoin;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;

import java.io.File;

public class Bitcoin {
    public static final String STORAGE_NAME_MAINNET = "storage_mainnet";
    public static final String STORAGE_NAME_TESTNET = "storage_testnet";

    public final File rootDirectory;
    public final File fileWallet;
    public final File fileSpvBlockchain;

    public final WalletAppKit kit;

    public Bitcoin() {
        this(TestNet3Params.get());
    }

    public Bitcoin(NetworkParameters params) {
        this(new File("."), params);
    }

    public Bitcoin(File rootDirectory, NetworkParameters params) {
        BriefLogFormatter.init();
        this.rootDirectory = rootDirectory;

        String filePrefix = params == MainNetParams.get() ? STORAGE_NAME_MAINNET : STORAGE_NAME_TESTNET;

        this.fileWallet = new File(rootDirectory, filePrefix + ".wallet");
        this.fileSpvBlockchain = new File(rootDirectory, filePrefix + ".spvchain");
        // Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
        kit = new WalletAppKit(params, rootDirectory, filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
            }
        };
    }

    public WalletAppKit startBlockchainAsync() {
        kit.startAsync();
        return kit;
    }

    public void cleanUp() {
        fileWallet.delete();
        fileSpvBlockchain.delete();
    }
}
