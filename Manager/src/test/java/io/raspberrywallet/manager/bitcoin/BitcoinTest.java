package io.raspberrywallet.manager.bitcoin;

import com.google.common.util.concurrent.Service;
import io.raspberrywallet.manager.TestUtils;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.TestNet3Params;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import static io.raspberrywallet.manager.Utils.println;
import static org.junit.jupiter.api.Assertions.*;

public class BitcoinTest {
    static private Bitcoin bitcoin;
    static private Service syncBlockchainService;

    @BeforeAll
    static void setup() {
        bitcoin = new Bitcoin();
        syncBlockchainService = bitcoin.startBlockchainAsync();
    }

    @Test
    void should_setup_test_net() {
        Bitcoin bitcoin = new Bitcoin(TestNet3Params.get());

        assertEquals(bitcoin.kit.params(), TestNet3Params.get());
        assertEquals(bitcoin.kit.directory(), bitcoin.rootDirectory);
    }


    @Test
    void should_sync_test_net() {
        syncBlockchainService.awaitRunning();

        assertEquals(bitcoin.params(), TestNet3Params.get());
        assertEquals(bitcoin.kit.directory(), bitcoin.rootDirectory);
        assertTrue(bitcoin.fileWallet.exists());
        assertTrue(bitcoin.fileSpvBlockchain.exists());

        bitcoin.fileSpvBlockchain.delete();
    }

    @Test
    void should_restore_randomly_generated_mnemonic_words() throws NoSuchAlgorithmException, MnemonicException {
        List<String> mnemonicCode = TestUtils.generateRandomDeterministicMnemonicCode();
        mnemonicCode.forEach(System.out::println);

        bitcoin.restoreFromBackupPhrase(mnemonicCode);
    }

    @Test
    void should_restore_private_key_bytes() throws NoSuchAlgorithmException {
        byte[] seed = SecureRandom.getInstanceStrong().generateSeed(32);

        // Importing keys is available only with blockchain synced
        syncBlockchainService.awaitRunning();

        bitcoin.importKey(seed);
        assertTrue(Arrays.equals(bitcoin.kit.wallet().getImportedKeys().get(0).getPrivKeyBytes(), seed));
        bitcoin.removeKey(seed);
    }


    @Test
    void getCurrentReceiveAddress() {
        syncBlockchainService.awaitRunning();
        String currentAddress = bitcoin.getCurrentReceiveAddress();

        assertNotNull(currentAddress);
        assertEquals(currentAddress.length(), 34);

        println(currentAddress);
    }

    @Test
    void getFreshReceiveAddress() {
        syncBlockchainService.awaitRunning();

        String freshAddress = bitcoin.getFreshReceiveAddress();
        assertNotNull(freshAddress);
        assertEquals(freshAddress.length(), 34);
        println(freshAddress);
    }

    @Test
    void getEstimatedBalance() {
        syncBlockchainService.awaitRunning();

        String balance = bitcoin.getEstimatedBalance();
        println(balance);
    }

    @Test
    void getAvailableBalance() {
        syncBlockchainService.awaitRunning();

        String availableBalance = bitcoin.getAvailableBalance();
        println(availableBalance);
    }


}