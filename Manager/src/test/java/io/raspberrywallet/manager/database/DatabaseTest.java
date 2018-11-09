package io.raspberrywallet.manager.database;

import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    private Database database = null;
    private String password = "changeit";

    private static KeyPartEntity exampleModuleKeypart = new KeyPartEntity();
    private static KeyPartEntity pushButtonModuleKeypart = new KeyPartEntity();

    @BeforeEach
    void setUp() throws DecryptionException, EncryptionException {
        try {
            File tempBaseDir = Paths.get("/", "tmp", "wallet").toFile();
            tempBaseDir.mkdirs();
            Configuration configuration = new Configuration(tempBaseDir.getAbsolutePath());

            database = new Database(configuration);
            database.setPassword(password);

            exampleModuleKeypart.setModule("ExampleModule");
            exampleModuleKeypart.setPayload("BGF$#Y%34".getBytes());

            pushButtonModuleKeypart.setModule("PinModule");
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

            database.initDatabase();
            int decryptedWalletHash = database.getWallet().hashCode();

            assertEquals(walletHash, decryptedWalletHash);

        } catch (EncryptionException | DecryptionException | IOException e) {
            fail(e.getMessage());
        }
    }

}