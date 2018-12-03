package io.raspberrywallet.manager.bitcoin;

import com.stasbar.Logger;
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

public class BitcoinJTest {
    private File rootDirectory = new File(".");
    private NetworkParameters params = TestNet3Params.get();
    private String walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();
    private File walletFile = new File(rootDirectory, walletFileName + ".wallet");
    private File blockStoreFile = new File(rootDirectory, walletFileName + ".spvchain");
    private String password = "password";

    @Test
    void initWalletFromSeed() throws Exception {
        List<String> mnemonicCode = Arrays.asList(
                "farm hospital shadow common raw neither pond access suggest army prefer expire".split(" "));

        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);

        KeyChainGroup keyChainGroup = new KeyChainGroup(params, seed);

        Wallet wallet = new Wallet(params, keyChainGroup); //Wallet.fromSeed(params, seed);

        synchronizeWalletBlocking(wallet);
        wallet.encrypt(password);
        wallet.saveToFile(walletFile);

        System.out.println("Wallet balance" + wallet.getBalance().toFriendlyString());
        restoreWalletFromFile();
    }

    @Test
    void restoreWalletFromFile() throws UnreadableWalletException, BlockStoreException, IOException {
        Wallet wallet = Wallet.loadFromFile(walletFile);

        if (!wallet.isEncrypted())
            throw new SecurityException("Decrypted wallet on disk detected");
        else
            wallet.decrypt(password);

        synchronizeWalletBlocking(wallet);

        wallet.encrypt(password);
        wallet.saveToFile(walletFile);

        Logger.info("Wallet balance" + wallet.getBalance().toFriendlyString());
    }


    void synchronizeWalletBlocking(Wallet wallet) throws BlockStoreException, IOException {
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
        peerGroup.stopAsync();
        blockStore.close();

        long total = System.currentTimeMillis() - start;
        Logger.info(String.format("Synchronization took %.2f secs", (double) total / 1000.0));
    }

    @Test
    void send_funds() {
        String destinationAddress = "mk7exmQTiXjqzAMaxzBSHYezjkNvJGNGHX";
        String amount = "0.0001";
        List<String> mnemonicCode = Arrays.asList(
                "farm hospital shadow common raw neither pond access suggest army prefer expire".split(" "));

        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);

        KeyChainGroup keyChainGroup = new KeyChainGroup(params, seed);

        Wallet wallet = new Wallet(params, keyChainGroup);

        Coin coinsAmount = Coin.parseCoin(amount);
        Address recipientAddress = Address.fromBase58(params, destinationAddress);
        try {
            wallet.sendCoins(SendRequest.to(recipientAddress, coinsAmount));
        } catch (InsufficientMoneyException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
        }
    }
}
