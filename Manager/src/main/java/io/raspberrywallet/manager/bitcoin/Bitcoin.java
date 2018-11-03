package io.raspberrywallet.manager.bitcoin;

import com.google.common.util.concurrent.ListenableFuture;
import com.stasbar.Logger;
import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.Configuration;
import lombok.Getter;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
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
import java.net.InetAddress;
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
    @Nullable
    private Wallet wallet;

    public Bitcoin(Configuration configuration) {
        BriefLogFormatter.init();
        this.bitcoinConfig = configuration.getBitcoinConfig();
        this.params = parseNetworkFrom(configuration.getBitcoinConfig());
        this.bitcoinRootDirectory = new File(configuration.getBasePathPrefix(), DIRECTORY_NAME);

        this.walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();

        this.walletFile = Paths.get(bitcoinRootDirectory.getAbsolutePath(), walletFileName + ".wallet").toFile();
        this.blockStoreFile = Paths.get(bitcoinRootDirectory.getAbsolutePath(), walletFileName + ".spvchain").toFile();
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

    public void setupWalletFromMnemonic(List<String> mnemonicCode, @Nullable KeyParameter key) {
        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);
        // Shut down synchronization and restart it with the new seed.
        Runnable setupWalletFromBackup = () -> {
            try {
                KeyChainGroup keyChainGroup = new KeyChainGroup(params, seed);

                wallet = new Wallet(params, keyChainGroup); //Wallet.fromSeed(params, seed);

                synchronizeWalletBlocking(wallet);

                if (key != null)
                    saveEncryptedWallet(key);

                System.out.println("Wallet balance" + wallet.getBalance().toFriendlyString());
            } catch (IOException | BlockStoreException | WalletNotInitialized walletNotInitialized) {
                walletNotInitialized.printStackTrace();
            }
        };

        if (peerGroup != null) {
            ListenableFuture future = peerGroup.stopAsync();
            future.addListener(setupWalletFromBackup, Executors.newSingleThreadExecutor());
        } else
            setupWalletFromBackup.run();
    }

    private void setupWalletFromFile(File walletFile, KeyParameter key) {
        // Shut down synchronization and restart it with the new seed.
        Runnable setupWalletFromBackup = () -> {
            try {
                wallet = Wallet.loadFromFile(walletFile);

                if (!wallet.isEncrypted()) {
                    saveEncryptedWallet(key);
                    throw new SecurityException("Decrypted wallet on disk detected");
                } else
                    decryptWallet(key);

                synchronizeWalletBlocking(wallet);

                saveEncryptedWallet(key);

                Logger.info("Wallet balance" + wallet.getBalance().toFriendlyString());
            } catch (IOException | BlockStoreException | UnreadableWalletException | WalletNotInitialized walletNotInitialized) {
                walletNotInitialized.printStackTrace();
            }
        };

        if (peerGroup != null) {
            ListenableFuture future = peerGroup.stopAsync();
            future.addListener(setupWalletFromBackup, Executors.newSingleThreadExecutor());
        } else
            setupWalletFromBackup.run();
    }


    private void synchronizeWalletBlocking(Wallet wallet) throws BlockStoreException, IOException {
        long start = System.currentTimeMillis();
        final SPVBlockStore blockStore = new SPVBlockStore(params, blockStoreFile);

        InputStream checkpoints = CheckpointManager.openStream(params);
        CheckpointManager.checkpoint(params, checkpoints, blockStore, wallet.getEarliestKeyCreationTime());

        BlockChain chain = new BlockChain(params, wallet, blockStore);

        PeerGroup peerGroup = new PeerGroup(params, chain);

        peerGroup.setUserAgent("RaspberryWallet", "1.0");
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.addWallet(wallet);
        peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        peerGroup.start();
        peerGroup.downloadBlockChain();

        long total = System.currentTimeMillis() - start;
        Logger.info(String.format("Synchronization took %.2f secs", (double) total / 1000.0));
    }

    @NotNull
    public Wallet getWallet() throws WalletNotInitialized {
        Wallet _wallet = wallet;
        if (_wallet == null) throw new WalletNotInitialized();
        return _wallet;
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

    public void saveEncryptedWallet(KeyParameter key) throws IOException, WalletNotInitialized {
        Wallet _wallet = wallet;
        if (_wallet == null) throw new WalletNotInitialized();
        encryptWallet(key);
        _wallet.saveToFile(walletFile);
        decryptWallet(key);
    }


    public void decryptWallet(KeyParameter key) throws WalletNotInitialized {
        Logger.d("Decrypt wallet with:" + new String(key.getKey()));
        getWallet().decrypt(key);
    }

    public void encryptWallet(KeyParameter key) throws WalletNotInitialized {
        Logger.d("Encrypt wallet with:" + new String(key.getKey()));
        Wallet wallet = getWallet();
        KeyCrypter keyCrypter = Optional
                .ofNullable(wallet.getKeyCrypter())
                .orElseGet(KeyCrypterScrypt::new);
        wallet.encrypt(keyCrypter, key);
    }
}
