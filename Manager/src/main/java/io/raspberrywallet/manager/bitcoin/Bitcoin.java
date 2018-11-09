package io.raspberrywallet.manager.bitcoin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.stasbar.Logger;
import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.Configuration;
import lombok.Getter;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.protocols.channels.StoredPaymentChannelClientStates;
import org.bitcoinj.protocols.channels.StoredPaymentChannelServerStates;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.jetbrains.annotations.NotNull;
import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * Class representing Bitcoin network, IO, key management API,
 * It uses WalletAppKit object composition pattern in order to hide unimportant functionality and safe extension.
 */
public class Bitcoin {
    private final static String DIRECTORY_NAME = "bitcoin";

    private final File bitcoinRootDirectory;
    private final String walletFileName;
    @Getter
    private final File walletFile;
    @Getter
    private final File blockStoreFile;

    private final NetworkParameters params;
    private final Configuration.BitcoinConfig bitcoinConfig;

    @Nullable
    @Getter
    private PeerGroup peerGroup;
    private SPVBlockStore blockStore;
    @Nullable
    private Wallet wallet;

    public Bitcoin(Configuration configuration) throws BlockStoreException, IOException {
        BriefLogFormatter.init();
        this.bitcoinConfig = configuration.getBitcoinConfig();
        this.params = parseNetworkFrom(configuration.getBitcoinConfig());

        this.bitcoinRootDirectory = Paths.get(configuration.getBasePathPrefix(), DIRECTORY_NAME).toFile();
        bitcoinRootDirectory.mkdirs();

        this.walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();

        this.walletFile = Paths.get(bitcoinRootDirectory.getAbsolutePath(), walletFileName + ".wallet").toFile();
        this.blockStoreFile = Paths.get(bitcoinRootDirectory.getAbsolutePath(), walletFileName + ".spvchain").toFile();
        if (isChainFileLocked())
            throw new IllegalStateException("This application is already running and cannot be started twice.");

        this.blockStore = new SPVBlockStore(params, blockStoreFile);
    }

    private NetworkParameters parseNetworkFrom(Configuration.BitcoinConfig bitcoinConfig) {
        switch (bitcoinConfig.getNetworkName()) {
            case "mainnet":
                return MainNetParams.get();
            case "testnet":
            default:
                return TestNet3Params.get();
        }
    }

    InputStream checkpoints;

    public void setupWalletFromMnemonic(List<String> mnemonicCode, @Nullable KeyParameter key) {
        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);
        Runnable setupWalletFromBackup = () -> {
            try {
                KeyChainGroup keyChainGroup = new KeyChainGroup(params, seed);
                wallet = new Wallet(params, keyChainGroup); //Wallet.fromSeed(params, seed);
                if (checkpoints == null)
                    checkpoints = CheckpointManager.openStream(params);

                if (checkpoints != null) {
                    // Initialize the chain file with a checkpoint to speed up first-run sync.
                    long time = seed.getCreationTimeSeconds();
                    removeOldBlockStore();
                    if (time > 0)
                        CheckpointManager.checkpoint(params, checkpoints, blockStore, wallet.getEarliestKeyCreationTime());
                    else
                        Logger.info("Creating a new uncheckpointed block store due to a wallet with a creation time of zero: this will result in a very slow chain sync");

                } else removeOldBlockStore();

                synchronizeWalletBlocking(wallet, key);

            } catch (IOException | BlockStoreException walletNotInitialized) {
                walletNotInitialized.printStackTrace();
            }
        };

        if (peerGroup != null) {
            // Shut down synchronization before setting up new wallet
            ListenableFuture future = peerGroup.stopAsync();
            future.addListener(setupWalletFromBackup, Executors.newSingleThreadExecutor());
        } else
            setupWalletFromBackup.run();
    }

    private void removeOldBlockStore() throws BlockStoreException, IOException {
        if (blockStoreFile.exists()) {
            Logger.info("Deleting the chain file in preparation from restore.");
            blockStore.close();

            if (!blockStoreFile.delete())
                throw new IOException("Failed to delete chain file in preparation for restore.");
            blockStore = new SPVBlockStore(params, blockStoreFile);
        }
    }

    public void setupWalletFromFile(@NotNull KeyParameter key) {
        Runnable setupWalletFromBackup = () -> {
            try {
                wallet = Wallet.loadFromFile(walletFile);

                if (!wallet.isEncrypted()) {
                    saveEncryptedWallet(key);
                    throw new SecurityException("Decrypted wallet on disk detected");
                } else
                    decryptWallet(key);

                synchronizeWalletBlocking(wallet, key);
            } catch (IOException | UnreadableWalletException | WalletNotInitialized walletNotInitialized) {
                walletNotInitialized.printStackTrace();
            }
        };
        if (peerGroup != null && peerGroup.isRunning()) {
            // Shut down synchronization before setting up new wallet
            ListenableFuture future = peerGroup.stopAsync();
            future.addListener(setupWalletFromBackup, Executors.newSingleThreadExecutor());
        } else
            setupWalletFromBackup.run();
    }


    /**
     * Download blockchain from PeerGroup discovered by DnsDiscovery on the NetworkParams
     *
     * @param wallet wallet to index transactions for
     */
    private void synchronizeWalletBlocking(final Wallet wallet, @Nullable KeyParameter key) throws IOException {

        BlockChain chain;
        try {
            chain = new BlockChain(params, blockStore);
        } catch (BlockStoreException e) {
            throw new IOException(e);
        }

        peerGroup = new PeerGroup(params, chain);
        peerGroup.setUserAgent("RaspberryWallet", "1.0");
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        chain.addWallet(wallet);
        peerGroup.addWallet(wallet);
        Futures.addCallback(peerGroup.startAsync(), new FutureCallback<Object>() {

            @Override
            public void onSuccess(@Nullable Object result) {
                completeExtensionInitiations(peerGroup, wallet);
                final long start = System.currentTimeMillis();
                DownloadProgressTracker listener = new DownloadProgressTracker() {
                    @Override
                    protected void doneDownload() {
                        long total = System.currentTimeMillis() - start;
                        Logger.info(String.format("Synchronization took %.2f secs", (double) total / 1000.0));
                        try {
                            if (key != null)
                                saveEncryptedWallet(key);
                            Logger.info("Wallet balance" + wallet.getBalance().toFriendlyString());
                        } catch (WalletNotInitialized | IOException walletNotInitialized) {
                            walletNotInitialized.printStackTrace();
                        }
                    }
                };
                peerGroup.startBlockChainDownload(listener);
            }

            @Override
            public void onFailure(Throwable t) {
                throw new RuntimeException();
            }
        });


    }

    @NotNull
    public Wallet getWallet() throws WalletNotInitialized {
        Wallet _wallet = wallet;
        if (_wallet == null) throw new WalletNotInitialized();
        return _wallet;
    }

    public void ensureWalletInitialized() throws WalletNotInitialized {
        if (wallet == null) throw new WalletNotInitialized();
    }

    /**
     * This method encrypt wallet, saves it onto disk and decrypt it back so it become again usable
     *
     * @param key key used to encrypt wallet before saving onto disk
     * @throws IOException          when the problem with saving wallet occurs
     * @throws WalletNotInitialized when you try to save not initialized wallet
     */
    public void saveEncryptedWallet(KeyParameter key) throws IOException, WalletNotInitialized {
        ensureWalletInitialized();

        encryptWallet(key);
        getWallet().saveToFile(walletFile);
        Logger.d("Saved wallet to: " + walletFile.getAbsolutePath());
        decryptWallet(key);
    }

    public void decryptWallet(KeyParameter key) throws WalletNotInitialized {
        Logger.d("Decrypting wallet with:" + new String(key.getKey()));
        getWallet().decrypt(key);
    }

    public void encryptWallet(KeyParameter key) throws WalletNotInitialized {
        Logger.d("Encrypting wallet with:" + new String(key.getKey()));
        Wallet wallet = getWallet();
        KeyCrypter keyCrypter = Optional
                .ofNullable(wallet.getKeyCrypter())
                .orElseGet(KeyCrypterScrypt::new);
        wallet.encrypt(keyCrypter, key);
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

    public void sendCoins(String amount, String recipient) throws WalletNotInitialized {
        Coin coinsAmount = Coin.parseCoin(amount);
        Address recipientAddress = Address.fromBase58(params, recipient);
        try {
            if (peerGroup == null) throw new WalletNotInitialized();
            getWallet().sendCoins(peerGroup, recipientAddress, coinsAmount);
        } catch (InsufficientMoneyException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isFirstTime() {
        return !walletFile.exists();
    }

    /**
     * Tests to see if the spvchain file has an operating system file lock on it. Useful for checking if your app
     * is already running. If another copy of your app is running and you start the appkit anyway, an exception will
     * be thrown during the startup process. Returns false if the chain file does not exist or is a directory.
     */
    public boolean isChainFileLocked() throws IOException {
        RandomAccessFile file2 = null;
        try {
            File file = new File(bitcoinRootDirectory, blockStoreFile.getName());
            if (!file.exists())
                return false;
            if (file.isDirectory())
                return false;
            file2 = new RandomAccessFile(file, "rw");
            FileLock lock = file2.getChannel().tryLock();
            if (lock == null)
                return true;
            lock.release();
            return false;
        } finally {
            if (file2 != null)
                file2.close();
        }
    }

    /*
     * As soon as the transaction broadcaster han been created we will pass it to the
     * payment channel extensions
     */
    private void completeExtensionInitiations(TransactionBroadcaster transactionBroadcaster, Wallet wallet) {
        StoredPaymentChannelClientStates clientStoredChannels = (StoredPaymentChannelClientStates)
                wallet.getExtensions().get(StoredPaymentChannelClientStates.class.getName());
        if (clientStoredChannels != null) {
            clientStoredChannels.setTransactionBroadcaster(transactionBroadcaster);
        }
        StoredPaymentChannelServerStates serverStoredChannels = (StoredPaymentChannelServerStates)
                wallet.getExtensions().get(StoredPaymentChannelServerStates.class.getName());
        if (serverStoredChannels != null) {
            serverStoredChannels.setTransactionBroadcaster(transactionBroadcaster);
        }
    }
}
