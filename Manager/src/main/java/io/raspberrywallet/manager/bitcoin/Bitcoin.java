package io.raspberrywallet.manager.bitcoin;

import com.google.common.util.concurrent.Service;
import com.stasbar.Logger;
import io.raspberrywallet.WalletNotInitialized;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
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
    final File rootDirectory;
    private final String walletFileName;
    private final NetworkParameters params;
    @Nullable
    private WalletAppKit kit;

    public Bitcoin() {
        this(TestNet3Params.get());
    }

    Bitcoin(NetworkParameters params) {
        this(new File("."), params);
    }

    private Bitcoin(File rootDirectory, NetworkParameters params) {
        BriefLogFormatter.init();
        this.params = params;
        this.rootDirectory = rootDirectory;
        this.walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();
    }

    private void setupWalletKit(@Nullable DeterministicSeed seed) {
        // If seed is non-null it means we are restoring from backup.
        kit = new WalletAppKit(params, rootDirectory, walletFileName) {
            @Override
            protected void onSetupCompleted() {
                Logger.info("Bitcoin setup complete");
                // Don't make the user wait for confirmations for now, as the intention is they're sending it
                // their own money!
                try {
                    getWallet().allowSpendingUnconfirmedTransactions();
                } catch (WalletNotInitialized walletNotInitialized) {
                    walletNotInitialized.printStackTrace();
                    throw new IllegalStateException("Wallet must be initialized at this point");
                }
            }
        };
        // Now configure and start the appkit. This will take a second or two - we could show a temporary splash screen
        // or progress widget to keep the user engaged whilst we initialise, but we don't.
        kit.setAutoSave(false)
                .setBlockingStartup(false)
                .setUserAgent("RaspberryWallet", "1.0");

        if (seed != null)
            kit.restoreWalletFromSeed(seed);
    }

    public WalletAppKit getKit() throws WalletNotInitialized {
        if (kit == null) throw new WalletNotInitialized();
        return kit;
    }

    public Wallet getWallet() throws WalletNotInitialized {
        if (kit == null) throw new WalletNotInitialized();
        return kit.wallet();
    }

    void importKey(byte[] keyBytes) throws WalletNotInitialized {
        importKey(ECKey.fromPrivate(keyBytes));
    }

    private void importKey(ECKey key) throws WalletNotInitialized {
        getWallet().importKey(key);
    }

    void removeKey(byte[] keyBytes) throws WalletNotInitialized {
        removeKey(ECKey.fromPrivate(keyBytes));
    }

    private void removeKey(ECKey key) throws WalletNotInitialized {
        getWallet().removeKey(key);
    }

    public String getFreshReceiveAddress() throws WalletNotInitialized {
        return getWallet().freshReceiveAddress().toBase58();
    }

    public String getCurrentReceiveAddress() throws WalletNotInitialized {
        return getWallet().currentReceiveAddress().toBase58();
    }

    /**
     * Balance calculated assuming all pending transactions are in fact included into the best chain by miners.
     * This includes the value of immature coinbase transactions.
     */
    public String getEstimatedBalance() throws WalletNotInitialized {
        return getWallet().getBalance(Wallet.BalanceType.ESTIMATED).toFriendlyString();
    }

    /**
     * Balance that could be safely used to create new spends, if we had all the needed private keys. This is
     * whatever the default coin selector would make available, which by default means transaction outputs with at
     * least 1 confirmation and pending transactions created by our own wallet which have been propagated across
     * the network. Whether we <i>actually</i> have the private keys or not is irrelevant for this balance type.
     */
    public String getAvailableBalance() throws WalletNotInitialized {
        return getWallet().getBalance(Wallet.BalanceType.AVAILABLE).toFriendlyString();
    }

    public void restoreFromSeed(List<String> mnemonicCode) {
        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);
        // Shut down synchronization and restart it with the new seed.
        Runnable setupWalletFromBackup = () -> {
            setupWalletKit(seed);
            try {
                getKit().startAsync();
            } catch (WalletNotInitialized walletNotInitialized) {
                walletNotInitialized.printStackTrace();
            }
        };
        try {
            getKit().addListener(new Service.Listener() {
                @Override
                public void terminated(Service.State from) {
                    setupWalletFromBackup.run();
                }
            }, Executors.newSingleThreadExecutor());
            getKit().stopAsync();
        } catch (WalletNotInitialized e) {
            setupWalletFromBackup.run();
        }
    }

    public void sendCoins(String amount, String recipient) throws WalletNotInitialized {
        Coin coinsAmount = Coin.parseCoin(amount);
        Address recipientAddress = Address.fromBase58(params, recipient);
        try {
            getWallet().sendCoins(getKit().peerGroup(), recipientAddress, coinsAmount);
        } catch (InsufficientMoneyException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();

        }
    }

    public File getWalletFile() {
        return new File(rootDirectory, walletFileName + ".wallet");
    }
}
