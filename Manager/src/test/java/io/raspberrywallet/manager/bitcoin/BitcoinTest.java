package io.raspberrywallet.manager.bitcoin;

import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.Configuration;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.store.BlockStoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static io.raspberrywallet.manager.Utils.println;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BitcoinTest {
    static private Bitcoin bitcoin;
    static private List<String> mnemonicCode;
    static private String privateKeyHash;

    @BeforeAll
    static void setup() throws BlockStoreException, IOException {
        mnemonicCode = Arrays.asList("member", "team", "romance", "alarm", "antique", "legal",
                "captain", "dutch", "matter", "dinner", "loan", "orange");
        println("Using mnemonic:" + mnemonicCode.stream().reduce("", (acc, word) -> acc + " " + word));
        // receive address mhTMbU8NqwVobEjT6Yqq3hSu9rmPABE1RU
        // balance 0.14001595
        privateKeyHash = new String(Sha256Hash.hash("rasperrywallet is the best bitcoin wallet ever".getBytes()));

        File tempBaseDir = Paths.get("/", "tmp", "wallet").toFile();
        tempBaseDir.mkdirs();
        Configuration configuration = new Configuration(tempBaseDir.getAbsolutePath());
        WalletCrypter walletCrypter = new WalletCrypter();
        bitcoin = new Bitcoin(configuration, walletCrypter);
    }

    @Test
    void use_case_restore_from_mnemonic_save_encrypted_and_restore_from_file() {
        should_restore_from_mnemonic_words();
        should_restore_from_file();
    }


    @Test
    void should_restore_from_mnemonic_words() {
        bitcoin.setupWalletFromMnemonic(mnemonicCode, privateKeyHash, true);
        bitcoin.getPeerGroup().stop();
    }

    @Test
    void should_restore_from_file() {
        bitcoin.setupWalletFromFile(privateKeyHash, true);
        bitcoin.getPeerGroup().stop();
    }


    @Test
    void getCurrentReceiveAddress() throws WalletNotInitialized {
        should_restore_from_file();
        String currentAddress = bitcoin.getCurrentReceiveAddress();

        assertNotNull(currentAddress);
        assertEquals(currentAddress.length(), 34);

        println(currentAddress);
    }

    @Test
    void getFreshReceiveAddress() throws WalletNotInitialized {
        should_restore_from_file();
        String freshAddress = bitcoin.getFreshReceiveAddress();
        assertNotNull(freshAddress);
        assertEquals(freshAddress.length(), 34);
        println(freshAddress);
    }

    @Test
    void getEstimatedBalance() throws WalletNotInitialized {
        should_restore_from_file();
        String balance = bitcoin.getEstimatedBalance();
        println(balance);
    }

    @Test
    void getAvailableBalance() throws WalletNotInitialized {
        should_restore_from_file();
        String availableBalance = bitcoin.getAvailableBalance();
        println(availableBalance);
    }


}