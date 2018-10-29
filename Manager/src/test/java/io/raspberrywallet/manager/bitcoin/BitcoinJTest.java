package io.raspberrywallet.manager.bitcoin;

import com.stasbar.Logger;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BitcoinJTest {
    private File rootDirectory = new File(".");
    private NetworkParameters params = TestNet3Params.get();
    private String walletFileName = "RaspberryWallet_" + params.getPaymentProtocolId();
    private File walletFile = new File(rootDirectory, walletFileName + ".wallet");
    private File blockstoreFile = new File(rootDirectory, walletFileName + ".spvchain");

    @Test
    void testWalletAppKit() {

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


    @Test
    void initWalletFromPrivateKey() throws BlockStoreException, UnknownHostException {
        ECKey key = new ECKey(new SecureRandom());
        String privateKey = Base58.encode(key.getPrivKeyBytes());
        System.out.println(privateKey);
        assertEquals(privateKey.length(),52);
        // Decode the private key from Satoshis Base58 variant. If 51 characters long then it's from Bitcoins
        // dumpprivkey command and includes a version byte and checksum, or if 52 characters long then it has
        // compressed pub key. Otherwise assume it's a raw key.
        NetworkParameters params = TestNet3Params.get();

        if (privateKey.length() == 51 || privateKey.length() == 52) {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privateKey);
            key = dumpedPrivateKey.getKey();
        } else {
            BigInteger privKey = Base58.decodeToBigInteger(privateKey);
            key = ECKey.fromPrivate(privKey);
        }
        Wallet wallet = new Wallet(params);
        wallet.importKey(key);

        // Find the transactions that involve those coins.
        final MemoryBlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, wallet, blockStore);

        PeerGroup peerGroup = new PeerGroup(params,chain);
        peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        peerGroup.startAsync();
        peerGroup.downloadBlockChain();
        peerGroup.stopAsync();

        // And take them!
        System.out.println("Wallet balance" + wallet.getBalance().toFriendlyString());

    }

    @Test
    void initWalletFromSeed() throws BlockStoreException, UnknownHostException {
        NetworkParameters params = TestNet3Params.get();
        List<String> mnemonicCode = Arrays.asList(
                "farm hospital shadow common raw neither pond access suggest army prefer expire".split(" "));

        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 1539388800);
        Wallet wallet = Wallet.fromSeed(params, seed);

        // Find the transactions that involve those coins.
        //final MemoryBlockStore blockStore = new MemoryBlockStore(params);
        final SPVBlockStore blockStore = new SPVBlockStore(params, blockstoreFile);
        BlockChain chain = new BlockChain(params, wallet, blockStore);

        PeerGroup peerGroup = new PeerGroup(params,chain);
        peerGroup.setUserAgent("RaspberryWallet", "1.0");
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.addAddress(new PeerAddress(params, InetAddress.getLocalHost()));
        peerGroup.startAsync();
        peerGroup.downloadBlockChain();
        peerGroup.stopAsync();

        // And take them!
        System.out.println("Wallet balance" + wallet.getBalance().toFriendlyString());


    }
    @Test
    void restoreWalletFromFile() throws UnreadableWalletException {
        Wallet wallet = Wallet.loadFromFile(walletFile);
    }
}
