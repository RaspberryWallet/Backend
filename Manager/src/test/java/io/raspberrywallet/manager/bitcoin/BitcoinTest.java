package io.raspberrywallet.manager.bitcoin;

import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.TestUtils;
import org.bitcoinj.crypto.MnemonicException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static io.raspberrywallet.manager.Utils.println;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BitcoinTest {
    static private Bitcoin bitcoin;


    @BeforeAll
    static void setup() {
        Configuration configuration = new Configuration();
        bitcoin = new Bitcoin(configuration);
    }


    @Test
    void should_restore_randomly_generated_mnemonic_words() throws NoSuchAlgorithmException, MnemonicException {
        List<String> mnemonicCode = TestUtils.generateRandomDeterministicMnemonicCode();
        mnemonicCode.forEach(System.out::println);

        bitcoin.setupWalletFromMnemonic(mnemonicCode, null);
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