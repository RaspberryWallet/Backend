package io.raspberrywallet.manager.database;

import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.cryptography.common.Password;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    private Database database = null;
    private Password password = new Password("mock passowrd".toCharArray());

    private static KeyPartEntity exampleModuleKeypart = new KeyPartEntity();
    private static KeyPartEntity pushButtonModuleKeypart = new KeyPartEntity();

    @BeforeEach
    void setUp() throws DecryptionException, EncryptionException {
        try {
            Configuration configuration = new Configuration();
            database = new Database(configuration, password);

            exampleModuleKeypart.setModule("io.raspberrywallet.manager.modules.example.ExampleModule");
            exampleModuleKeypart.setPayload("BGF$#Y%34".getBytes());

            pushButtonModuleKeypart.setModule("io.raspberrywallet.manager.modules.pushbutton.PushButtonModule");
            pushButtonModuleKeypart.setPayload("$TN$@C54B".getBytes());

            database.addKeyPart(exampleModuleKeypart);
            database.addKeyPart(pushButtonModuleKeypart);

            assertNotNull(database);
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

    @Test
    void DatabaseEncryptionThenDecryptionWorks() {
        try {
            int walletHash = database.getWallet().hashCode();
            database.saveWallet();

            // encrypted copy should still exists in file
            database.destroy();

            database.loadDatabase();
            int decryptedWalletHash = database.getWallet().hashCode();

            assertEquals(walletHash, decryptedWalletHash);

        } catch (EncryptionException | DecryptionException | IOException e) {
            fail(e.getMessage());
        }
    }

}