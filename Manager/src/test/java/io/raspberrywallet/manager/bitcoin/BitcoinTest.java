package io.raspberrywallet.manager.bitcoin;

import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.TestUtils;
import org.bitcoinj.crypto.MnemonicException;
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


    @BeforeAll
    static void setup() {
        bitcoin = new Bitcoin();
    }


    @Test
    void should_restore_randomly_generated_mnemonic_words() throws NoSuchAlgorithmException, MnemonicException, WalletNotInitialized {
        List<String> mnemonicCode = TestUtils.generateRandomDeterministicMnemonicCode();
        mnemonicCode.forEach(System.out::println);

        bitcoin.restoreFromSeed(mnemonicCode);
    }

    @Test
    void should_restore_private_key_bytes() throws NoSuchAlgorithmException, WalletNotInitialized {
        byte[] seed = SecureRandom.getInstanceStrong().generateSeed(32);

        // Importing keys is available only with blockchain synced

        bitcoin.importKey(seed);
        assertTrue(Arrays.equals(bitcoin.getWallet().getImportedKeys().get(0).getPrivKeyBytes(), seed));
        bitcoin.removeKey(seed);
    }


    @Test
    void getCurrentReceiveAddress() throws WalletNotInitialized {
        String currentAddress = bitcoin.getCurrentReceiveAddress();

        assertNotNull(currentAddress);
        assertEquals(currentAddress.length(), 34);

        println(currentAddress);
    }

    @Test
    void getFreshReceiveAddress() throws WalletNotInitialized {
        String freshAddress = bitcoin.getFreshReceiveAddress();
        assertNotNull(freshAddress);
        assertEquals(freshAddress.length(), 34);
        println(freshAddress);
    }

    @Test
    void getEstimatedBalance() throws WalletNotInitialized {
        String balance = bitcoin.getEstimatedBalance();
        println(balance);
    }

    @Test
    void getAvailableBalance() throws WalletNotInitialized {
        String availableBalance = bitcoin.getAvailableBalance();
        println(availableBalance);
    }


}