package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mchange.v1.util.ArrayUtils;
import org.bitcoinj.wallet.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    public Database db = null;

    public static final String ADDRESS_1 = "11113333399999AAAAABBBBB";
    public static final String ADDRESS_2 = "5+CVB12312D35143AFE$FAF==";
    public static final double BALANCE_1 = 0.12391;
    public static final double BALANCE_2 = 8219312.4342;
    public static final byte[] KEYPART_1_1 = new byte[] {11, 22, 33, 44};
    public static final byte[] KEYPART_1_2 = "#$#$^$^!#".getBytes();
    public static final byte[] KEYPART_2_1 = new byte[] {0, 0, 0, 0};
    public static final String KEYPART_1_1_MODULE = "io.raspberrywallet.manager.modules.ExampleModule";
    public static final String KEYPART_1_2_MODULE = "io.raspberrywallet.manager.modules.PushButtonModule";
    public static final String KEYPART_2_1_MODULE = "io.raspberrywallet.manager.modules.ExampleModule";

    public static WalletEntity walletEntity1 = null;
    public static WalletEntity walletEntity2 = null;
    public static KeyPartEntity keyPartEntity1_1 = new KeyPartEntity();
    public static KeyPartEntity keyPartEntity1_2 = new KeyPartEntity();
    public static KeyPartEntity keyPartEntity2_1 = new KeyPartEntity();

    public static byte[] serializedData = null;
    public static byte[] encrypted = null;

    @BeforeEach
    void setUp() {
        try {
            db = new Database(true);
            walletEntity1 = new WalletEntity();
            walletEntity2 = new WalletEntity();

            walletEntity1.address = ADDRESS_1;
            walletEntity1.balance = BALANCE_1;
            walletEntity2.address = ADDRESS_2;
            walletEntity2.balance = BALANCE_2;

            keyPartEntity1_1.order=1;
            keyPartEntity1_1.payload = KEYPART_1_1;
            keyPartEntity1_1.module = KEYPART_1_1_MODULE;

            keyPartEntity1_2.order = 2;
            keyPartEntity1_2.payload = KEYPART_1_2;
            keyPartEntity1_2.module = KEYPART_1_2_MODULE;

            keyPartEntity2_1.order = 1;
            keyPartEntity2_1.payload = KEYPART_2_1;
            keyPartEntity2_1.module = KEYPART_2_1_MODULE;

            walletEntity1.parts.add(keyPartEntity1_1);
            walletEntity1.parts.add(keyPartEntity1_2);
            walletEntity2.parts.add(keyPartEntity2_1);

            db.addWallets(Arrays.asList(walletEntity1, walletEntity2));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assertNotNull(db);
        }
    }



    @Test
    void serialize() {

        serializedData = null;

        try {
            serializedData = db.getSerialized();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {
            assertNotNull(serializedData);
        }
    }

    @Test
    void deserialize() {
        serialize();

        List<WalletEntity> newWallets = null;
        assertNotNull(serializedData);
        try {
            newWallets = db.deserialize(serializedData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assertNotNull(newWallets);
        }

        List<WalletEntity> oldWallets = Arrays.asList(walletEntity1, walletEntity2);
        assertEquals(oldWallets.size(), newWallets.size());

        /*
        * `List::containsAll` uses `Object::equals` to determine whether a collection contains another.
        * This condition checks if `List`s are equal using overridden `Object::equal`.
        * */
        assertTrue(oldWallets.containsAll(newWallets) && newWallets.containsAll(oldWallets));
    }

    @Test
    void encrypt() {

        db.addWallets(Arrays.asList(walletEntity1, walletEntity2));

        try {
            encrypted = db.encrypt(db.getSerialized());
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            assertNotNull(encrypted);
        }
    }

    @Test
    void decrypt() {
        encrypt();

        assertNotNull(encrypted);

        byte[] decrypted = null;
        try {
            decrypted = db.decrypt(encrypted);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            assertNotNull(decrypted);
        }
    }
}