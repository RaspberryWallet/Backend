package io.raspberrywallet.manager.bitcoin;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.stasbar.Logger;
import io.raspberrywallet.contract.CommunicationChannel;
import io.raspberrywallet.contract.IncorrectPasswordException;
import io.raspberrywallet.contract.TransactionView;
import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.Configuration;
import lombok.Getter;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
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

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.DoubleConsumer;

import static java.util.stream.Collectors.toList;

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
    private Wallet wallet;
    @Nullable
    @Getter
    private PeerGroup peerGroup;
    private SPVBlockStore blockStore;
    private InputStream checkpoints;
    private DoubleConsumer blockchainProgressListener;
    private final WalletCrypter walletCrypter;
    private CommunicationChannel frontendChannel;

    public Bitcoin(Configuration configuration, @NotNull WalletCrypter walletCrypter, CommunicationChannel frontendChannel) throws BlockStoreException, IOException {
        BriefLogFormatter.init();
        this.walletCrypter = walletCrypter;
        this.bitcoinConfig = configuration.getBitcoinConfig();
        this.params = parseNetworkFrom(configuration.getBitcoinConfig());
        this.frontendChannel = frontendChannel;
        this.bitcoinRootDirectory = Paths.get(configuration.getBasePathPrefix(), DIRECTORY_NAME).toFile();
        bitcoinRootDirectory.mkdirs();

        this.walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();

        this.walletFile = Paths.get(bitcoinRootDirectory.getAbsolutePath(), walletFileName + ".wallet").toFile();
        this.blockStoreFile = Paths.get(bitcoinRootDirectory.getAbsolutePath(), walletFileName + ".spvchain").toFile();
        if (isChainFileLocked())
            throw new IllegalStateException("This application is already running and cannot be started twice. " +
                    "\nPlease check what process is using blockstore by executing" +
                    "\nfuser " + blockStoreFile.getAbsolutePath() + "\nor \nlsof " + blockStoreFile.getAbsolutePath());

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

    public void setupWalletFromMnemonic(List<String> mnemonicCode, @Nullable String password) {
        setupWalletFromMnemonic(mnemonicCode, password, false);
    }

    void setupWalletFromMnemonic(List<String> mnemonicCode, @Nullable String password, boolean blocking) {
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

                synchronizeWalletNonBlocking(wallet, password, blocking);

            } catch (IOException | BlockStoreException e) {
                e.printStackTrace();
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

    public void setupWalletFromFile(@NotNull String password) {
        setupWalletFromFile(password, false);
    }

    void setupWalletFromFile(@NotNull String password, boolean blocking) {
        Runnable setupWalletFromBackup = () -> {
            try {
                wallet = Wallet.loadFromFile(walletFile);

                if (!wallet.isEncrypted()) {
                    throw new SecurityException("Decrypted wallet on disk detected");
                } else
                    decryptWallet(wallet, password);

                synchronizeWalletNonBlocking(wallet, password, blocking);
            } catch (IOException | UnreadableWalletException | IllegalArgumentException e) {
                e.printStackTrace();
                frontendChannel.error(e.getMessage());
            } catch (IncorrectPasswordException e) {
                throw new RuntimeException(e);
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
    private void synchronizeWalletNonBlocking(final Wallet wallet, @Nullable String password, boolean blocking) throws IOException {
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
        Runnable afterSynchronizationComplete = () -> {
            if (password != null) {
                try {
                    saveEncryptedWallet(password);
                    Logger.info("Wallet balance" + wallet.getBalance().toFriendlyString());
                    Logger.info("Wallet address" + wallet.currentReceiveAddress().toBase58());
                } catch (IOException | WalletNotInitialized e) {
                    frontendChannel.error(e.getMessage());
                    e.printStackTrace();
                } catch (IncorrectPasswordException e) {
                    frontendChannel.error("Failed to save wallet " + e.getMessage());
                }
            }

        };
        Futures.addCallback(peerGroup.startAsync(), new FutureCallback<Object>() {

            @Override
            public void onSuccess(@Nullable Object result) {
                completeExtensionInitiations(peerGroup, wallet);
                DownloadProgressTracker listener = new DownloadProgressTracker() {
                    @Override
                    protected void progress(double pct, int blocksSoFar, Date date) {
                        super.progress(pct, blocksSoFar, date);
                        Logger.d("Progress " + pct + "%");
                        if (blockchainProgressListener != null) {
                            blockchainProgressListener.accept(pct);
                            Logger.d("blockchainProgressListener not null, sending " + pct);
                        }
                    }

                    @Override
                    protected void doneDownload() {
                        afterSynchronizationComplete.run();
                    }
                };
                if (blocking) {
                    peerGroup.downloadBlockChain();
                    afterSynchronizationComplete.run();
                } else
                    peerGroup.startBlockChainDownload(listener);
            }

            @Override
            public void onFailure(Throwable t) {
                throw new RuntimeException(t);
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
     * @param password used to encrypt wallet before saving onto disk
     * @throws IOException          when the problem with saving wallet occurs
     * @throws WalletNotInitialized when you try to save not initialized wallet
     */
    public void saveEncryptedWallet(String password) throws IOException, WalletNotInitialized, IncorrectPasswordException {
        saveEncryptedWallet(getWallet(), password);
    }

    private void saveEncryptedWallet(@NotNull Wallet wallet, String password) throws IOException, IncorrectPasswordException {
        encryptWallet(wallet, password);
        wallet.saveToFile(walletFile);
        Logger.d("Saved wallet to: " + walletFile.getAbsolutePath());
        decryptWallet(wallet, password);
    }

    public void encryptWallet(String password) throws WalletNotInitialized, IncorrectPasswordException {
        encryptWallet(getWallet(), password);
    }

    private void encryptWallet(Wallet wallet, String password) throws IncorrectPasswordException {
        walletCrypter.encryptWallet(wallet, password);
    }

    public void decryptWallet(String password) throws WalletNotInitialized, IncorrectPasswordException {
        decryptWallet(getWallet(), password);
    }

    private void decryptWallet(@NotNull Wallet wallet, String password) throws IncorrectPasswordException {
        walletCrypter.decryptWallet(wallet, password);
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

    /**
     * @param amount    in BTC unit
     * @param recipient base58 address
     */
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

    /**
     * @return list of transactions related with this wallet
     */
    public List<TransactionView> getAllTransactions() throws WalletNotInitialized {
        final Wallet wallet = getWallet();
        final ArrayList<Transaction> transactions = new ArrayList<>(wallet.getTransactions(false));

        return transactions.stream().map(tx -> {
                    TransactionView txView = new TransactionView();
                    txView.setTxHash(tx.getHashAsString());
                    txView.setCreationTimestamp(tx.getUpdateTime().getTime());

                    final Address toAddress = tx.getOutput(0).getAddressFromP2PKHScript(TestNet3Params.get());
                    if (toAddress != null) txView.setToAddress(toAddress.toString());

                    txView.setAmountFromMe(tx.getValueSentFromMe(wallet).toFriendlyString());
                    txView.setAmountToMe(tx.getValueSentToMe(wallet).toFriendlyString());

                    long fee = (tx.getInputSum().getValue() > 0 ? tx.getInputSum().getValue() - tx.getOutputSum().getValue() : 0);
                    txView.setFee(Coin.valueOf(fee).toFriendlyString());

                    txView.setConfirmations(tx.getConfidence().getDepthInBlocks());
                    return txView;
                }
        ).sorted(Comparator.comparing(TransactionView::getCreationTimestamp))
                .collect(toList());
    }

    public boolean isFirstTime() {
        return !walletFile.exists();
    }

    /**
     * Tests to see if the spvchain file has an operating system file lock on it. Useful for checking if your app
     * is already running. If another copy of your app is running and you start the appkit anyway, an exception will
     * be thrown during the startup process. Returns false if the chain file does not exist or is a directory.
     */
    private boolean isChainFileLocked() throws IOException {
        RandomAccessFile accessFile = null;
        try {
            File blockStoreFile = new File(bitcoinRootDirectory, this.blockStoreFile.getName());
            if (!blockStoreFile.exists() || blockStoreFile.isDirectory())
                return false;
            accessFile = new RandomAccessFile(blockStoreFile, "rw");
            FileLock lock = accessFile.getChannel().tryLock();
            if (lock == null)
                return true;
            lock.release();
            return false;
        } finally {
            if (accessFile != null)
                accessFile.close();
        }
    }

    /**
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

    public void addBlockChainProgressListener(DoubleConsumer blockchainProgressListener) {
        this.blockchainProgressListener = blockchainProgressListener;
    }
}
