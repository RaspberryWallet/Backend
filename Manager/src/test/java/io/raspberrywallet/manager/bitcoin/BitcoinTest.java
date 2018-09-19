package io.raspberrywallet.manager.bitcoin;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Test;

class BitcoinTest {

    @Test
    void should_setupTestNet() {
        Bitcoin bitcoin = new Bitcoin(TestNet3Params.get());

        assert bitcoin.kit.params() == TestNet3Params.get();
        assert bitcoin.kit.directory() == bitcoin.rootDirectory;
        bitcoin.cleanup();
    }


    @Test
    void should_syncTestNet() {
        Bitcoin bitcoin = new Bitcoin();
        WalletAppKit kit = bitcoin.kit;

        bitcoin.startBlockchainAsync().awaitRunning();

        assert kit.params() == TestNet3Params.get();
        assert kit.directory() == bitcoin.rootDirectory;
        assert bitcoin.fileWallet.exists();
        assert bitcoin.fileSpvBlockchain.exists();
        bitcoin.cleanup();
    }
}