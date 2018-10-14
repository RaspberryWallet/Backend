package io.raspberrywallet.manager.bitcoin;

import com.google.common.util.concurrent.Service;
import com.stasbar.Logger;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

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
    public final NetworkParameters params;
    public WalletAppKit kit;

    public Bitcoin() {
        this(TestNet3Params.get());
    }

    public Bitcoin(NetworkParameters params) {
        this(new File("."), params);
    }

    public Bitcoin(File rootDirectory, NetworkParameters params) {
        BriefLogFormatter.init();
        this.params = params;
        this.rootDirectory = rootDirectory;

        String filePrefix = params == MainNetParams.get() ? STORAGE_NAME_MAINNET : STORAGE_NAME_TESTNET;

        this.fileWallet = new File(rootDirectory, filePrefix + ".wallet");
        this.fileSpvBlockchain = new File(rootDirectory, filePrefix + ".spvchain");
        setupWalletKit(null);
    }

    private void setupWalletKit(@Nullable DeterministicSeed seed) {
        String filePrefix = params == MainNetParams.get() ? STORAGE_NAME_MAINNET : STORAGE_NAME_TESTNET;
        // If seed is non-null it means we are restoring from backup.
        kit = new WalletAppKit(params, rootDirectory, filePrefix) {
            @Override
            protected void onSetupCompleted() {
                // Don't make the user wait for confirmations for now, as the intention is they're sending it
                // their own money!
                kit.wallet().allowSpendingUnconfirmedTransactions();
                Logger.info("Bitcoin setup complete");
            }
        };
        // Now configure and start the appkit. This will take a second or two - we could show a temporary splash screen
        // or progress widget to keep the user engaged whilst we initialise, but we don't.
        kit.setAutoSave(false);
        kit.setBlockingStartup(false)
                .setUserAgent("RaspberryWallet", "1.0");

        if (seed != null)
            kit.restoreWalletFromSeed(seed);
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

    public void restoreFromSeed(byte[] entropy) {
        DeterministicSeed seed = new DeterministicSeed(entropy, "", 1539388800);
        Logger.d("restoreFromSeedEntropy: " + seed.toString());
        // Shut down bitcoinj and restart it with the new seed.
        kit.addListener(new Service.Listener() {
            @Override
            public void terminated(Service.State from) {
                setupWalletKit(seed);
                kit.startAsync();
            }
        }, Executors.newSingleThreadExecutor());
        kit.stopAsync();

    }

    public byte[] restoreFromSeed(List<String> mnemonicCode) {
        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);
        Logger.d("restoreFromSeedwords: " + seed.toString());
        // Shut down bitcoinj and restart it with the new seed.
        kit.addListener(new Service.Listener() {
            @Override
            public void terminated(Service.State from) {
                setupWalletKit(seed);
                kit.startAsync();
            }
        }, Executors.newSingleThreadExecutor());
        kit.stopAsync();
        return seed.getSeedBytes();
    }

    public void sendCoins(String amount, String recipient) {
        Coin coinsAmount = Coin.parseCoin(amount);
        Address recipientAddress = Address.fromBase58(params, recipient);
        try {
            kit.wallet().sendCoins(kit.peerGroup(), recipientAddress, coinsAmount);
        } catch (InsufficientMoneyException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();

        }
    }
}
