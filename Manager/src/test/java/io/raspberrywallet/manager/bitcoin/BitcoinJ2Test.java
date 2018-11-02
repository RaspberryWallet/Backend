package io.raspberrywallet.manager.bitcoin;

import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

public class BitcoinJ2Test {
    private File rootDirectory = new File(".");
    private NetworkParameters params = TestNet3Params.get();
    private String walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();
    private File walletFile = new File(rootDirectory, walletFileName + ".wallet");
    private File blockstoreFile = new File(rootDirectory, walletFileName + ".spvchain");

    @Test
    void initWalletFromSeed() throws Exception {
        NetworkParameters params = TestNet3Params.get();
        List<String> mnemonicCode = Arrays.asList(
                "farm hospital shadow common raw neither pond access suggest army prefer expire".split(" "));

        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);
        KeyChainGroup keyChainGroup = new KeyChainGroup(params, seed);

        Wallet wallet = new Wallet(params, keyChainGroup);
        wallet.saveToFile(walletFile);
        boolean chainFileExists = blockstoreFile.exists();
        boolean shouldReplayWallet = (walletFile.exists() && !chainFileExists) || mnemonicCode != null;
        wallet = loadWallet();
        // Find the transactions that involve those coins.
        //final MemoryBlockStore blockStore = new MemoryBlockStore(params);
        final SPVBlockStore blockStore = new SPVBlockStore(params, blockstoreFile);
        InputStream checkpoints = null;
        if (checkpoints == null) {
            checkpoints = CheckpointManager.openStream(params);
        }

        CheckpointManager.checkpoint(params, checkpoints, blockStore, seed.getCreationTimeSeconds());
        BlockChain chain = new BlockChain(params, wallet, blockStore);

        PeerGroup peerGroup = new PeerGroup(params, chain);

        peerGroup.setUserAgent("RaspberryWallet", "1.0");
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.addWallet(wallet);
        peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        peerGroup.start();
        peerGroup.downloadBlockChain();
        peerGroup.stopAsync();

        // And take them!
        System.out.println("Wallet balance" + wallet.getBalance().toFriendlyString());
    }


    private Wallet loadWallet() throws Exception {
        Wallet wallet;
        FileInputStream walletStream = new FileInputStream(walletFile);
        try {
            WalletExtension[] extArray = new WalletExtension[]{};
            Protos.Wallet proto = WalletProtobufSerializer.parseToProto(walletStream);
            final WalletProtobufSerializer serializer;
            serializer = new WalletProtobufSerializer();
            wallet = serializer.readWallet(params, extArray, proto);
        } finally {
            walletStream.close();
        }
        return wallet;
    }

    @Test
    void restoreWalletFromFile() throws UnreadableWalletException {
        Wallet wallet = Wallet.loadFromFile(walletFile);
    }
}
