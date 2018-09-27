package io.raspberrywallet.manager.bitcoin;

import com.google.common.util.concurrent.Service;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.util.List;

/**
 * Class representing Bitcoin network, IO, key management API,
 * It uses WalletAppKit object composition pattern in order to hide unimportant functionality and safe extension.
 */
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
        //We don't want it to auto save (security leak)
        kit.setAutoSave(false);
    }

    public Service startBlockchainAsync() {
        return kit.startAsync();
    }

    public NetworkParameters params() {
        return kit.params();
    }

    public boolean importKey(byte[] keyBytes) {
        return importKey(ECKey.fromPrivate(keyBytes));
    }

    public boolean importKey(ECKey key) {
        return kit.wallet().importKey(key);
    }

    public boolean removeKey(byte[] keyBytes) {
        return removeKey(ECKey.fromPrivate(keyBytes));
    }

    public boolean removeKey(ECKey key) {
        return kit.wallet().removeKey(key);
    }

    public String getFreshReceiveAddress() {
        return kit.wallet().freshReceiveAddress().toBase58();
    }

    public String getCurrentReceiveAddress() {
        return kit.wallet().currentReceiveAddress().toBase58();
    }

    /**
     * Balance calculated assuming all pending transactions are in fact included into the best chain by miners.
     * This includes the value of immature coinbase transactions.
     */
    public String getEstimatedBalance() {
        return kit.wallet().getBalance(Wallet.BalanceType.ESTIMATED).toFriendlyString();
    }

    /**
     * Balance that could be safely used to create new spends, if we had all the needed private keys. This is
     * whatever the default coin selector would make available, which by default means transaction outputs with at
     * least 1 confirmation and pending transactions created by our own wallet which have been propagated across
     * the network. Whether we <i>actually</i> have the private keys or not is irrelevant for this balance type.
     */
    public String getAvailableBalance() {
        return kit.wallet().getBalance(Wallet.BalanceType.AVAILABLE).toFriendlyString();
    }

    public void cleanUp() {
        fileWallet.delete();
        fileSpvBlockchain.delete();
    }

    public void restoreFromBackupPhrase(List<String> mnemonicCode) {
        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 0L);
        kit.restoreWalletFromSeed(seed);
    }

}
