package io.raspberrywallet.manager.bitcoin;

import io.raspberrywallet.contract.CommunicationChannel;
import io.raspberrywallet.contract.TransactionView;
import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.Configuration;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.store.BlockStoreException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.raspberrywallet.manager.Utils.println;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Long running and resource consuming tests")
public class BitcoinTest {
    static private Bitcoin bitcoin;
    static private List<String> mnemonicCode;
    static private String privateKeyHash;

    @BeforeAll
    static void setup() throws BlockStoreException, IOException {
        String mnemonicWords = "member team romance alarm antique legal captain dutch matter dinner loan orange";
        mnemonicCode = Arrays.asList(mnemonicWords.split(" "));
        println("Using mnemonic:" + mnemonicCode.stream().reduce("", (acc, word) -> acc + " " + word));
        // receive address mhTMbU8NqwVobEjT6Yqq3hSu9rmPABE1RU
        // balance 0.15001595
        privateKeyHash = new String(Sha256Hash.hash("rasperrywallet is the best bitcoin wallet ever".getBytes()));

        Configuration configuration = Configuration.testConfiguration();
        WalletCrypter walletCrypter = new WalletCrypter();
        CommunicationChannel communicationChannel = new CommunicationChannel();
        bitcoin = new Bitcoin(configuration, walletCrypter, communicationChannel);
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
        if (bitcoin.getWalletFile().length() > 0)
            bitcoin.setupWalletFromFile(privateKeyHash, true);
        else
            bitcoin.setupWalletFromMnemonic(mnemonicCode, privateKeyHash, true);

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
        if (bitcoin.getWalletFile().length() > 0)
            bitcoin.setupWalletFromFile(privateKeyHash, true);
        else
            bitcoin.setupWalletFromMnemonic(mnemonicCode, privateKeyHash, true);
        String balance = bitcoin.getEstimatedBalance();
        println(balance);
    }

    @Test
    void getAvailableBalance() throws WalletNotInitialized {
        if (bitcoin.getWalletFile().length() > 0)
            bitcoin.setupWalletFromFile(privateKeyHash, true);
        else
            bitcoin.setupWalletFromMnemonic(mnemonicCode, privateKeyHash, true);
        String availableBalance = bitcoin.getAvailableBalance();
        println(availableBalance);
    }

    @Test
    void sendFunds() throws WalletNotInitialized {
        if (bitcoin.getWalletFile().length() > 0)
            bitcoin.setupWalletFromFile(privateKeyHash, true);
        else
            bitcoin.setupWalletFromMnemonic(mnemonicCode, privateKeyHash, true);
        String destinationAddress = "mk7exmQTiXjqzAMaxzBSHYezjkNvJGNGHX";
        String amount = "0.0001";

        bitcoin.sendCoins(amount, destinationAddress);
    }

    @Test
    void printTransactions() throws WalletNotInitialized {
        if (bitcoin.getWalletFile().length() > 0)
            bitcoin.setupWalletFromFile(privateKeyHash, true);
        else
            bitcoin.setupWalletFromMnemonic(mnemonicCode, privateKeyHash, true);

        final List<TransactionView> transactions = bitcoin.getAllTransactions();

        int i = 1;
        for (TransactionView tx : transactions) {
            System.out.println(i + "  ________________________");
            println(tx.toString());
            i++;
        }
    }

}