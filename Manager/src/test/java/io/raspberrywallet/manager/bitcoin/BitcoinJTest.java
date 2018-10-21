package io.raspberrywallet.manager.bitcoin;

import com.stasbar.Logger;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class BitcoinJTest {
    private File rootDirectory = new File(".");
    private NetworkParameters params = TestNet3Params.get();
    private String walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();

    @Test
    void testWalletAppKit() {
        File walletFile = getWalletFile();
        if (walletFile.exists()) walletFile.delete();
        assertFalse(walletFile.exists());

        WalletAppKit kit = new WalletAppKit(params, rootDirectory, walletFileName) {
            @Override
            protected void onSetupCompleted() {
                Logger.info("Bitcoin setup complete");
                assertFalse(walletFile.exists());
            }
        };

        kit.setAutoSave(false);
        kit.startAsync().awaitRunning();
    }


    public File getWalletFile() {
        return new File(rootDirectory, walletFileName + ".wallet");
    }
}
