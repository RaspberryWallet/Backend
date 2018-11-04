package io.raspberrywallet.manager.bitcoin;

import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.Configuration;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.store.BlockStoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.crypto.params.KeyParameter;

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
    static private KeyParameter key;

    @BeforeAll
    static void setup() throws BlockStoreException, IOException {
        mnemonicCode = Arrays.asList("member", "team", "romance", "alarm", "antique", "legal",
                "captain", "dutch", "matter", "dinner", "loan", "orange");
        println("Using mnemonic:" + mnemonicCode.stream().reduce("", (acc, word) -> acc + " " + word));

        key = new KeyParameter(Sha256Hash.hash("rasperrywallet is the best bitcoin wallet ever".getBytes()));

        Paths.get("/", "tmp", "wallet").toFile().mkdirs();
        Configuration configuration = new Configuration(360000, "/tmp/wallet", "1.0");
        bitcoin = new Bitcoin(configuration);
    }

    @Test
    void use_case_restore_from_mnemonic_save_encrypted_and_restore_from_file() {
        should_restore_from_mnemonic_words();
        should_restore_from_file();
    }


    @Test
    void should_restore_from_mnemonic_words() {
        bitcoin.setupWalletFromMnemonic(mnemonicCode, key);
        bitcoin.getPeerGroup().stop();
    }

    @Test
    void should_restore_from_file() {
        bitcoin.setupWalletFromFile(key);
        bitcoin.getPeerGroup().stop();
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