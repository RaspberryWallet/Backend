package io.raspberrywallet.manager;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationTest {
    @Test
    void fromYamlFile() {
        Configuration configuration = Configuration.fromYamlFile(new File("../config.yaml"));
        assertNotNull(configuration.getVersion());
        assertNotNull(configuration.getModulesConfig());
        assertNotNull(configuration.getWalletConfig());
    }

    @Test
    void getSessionLength() {
        final int sessionLength = 36000;
        Configuration configuration = new Configuration(sessionLength, "/opt/wallet", "1.0.0");
        assertEquals(sessionLength, configuration.getSessionLength());

    }

    @Test
    void getBasePathPrefix() {
        final String basePathPrefix = "/opt/wallet";
        Configuration configuration = new Configuration(36000, basePathPrefix, "1.0.0");
        assertEquals(basePathPrefix, configuration.getBasePathPrefix());
    }

    @Test
    void getVersion() {
        final String version = "1.0.0";
        Configuration configuration = new Configuration(36000, "/opt/wallet", version);
        assertEquals(version, configuration.getVersion());
    }

    @Test
    void getWalletConfig() {
        Configuration.WalletConfiguration walletConfig = new Configuration.WalletConfiguration();
        long autoLockingTime = 10000;
        walletConfig.setAutoLockTime(autoLockingTime);

        Configuration configuration = new Configuration(walletConfig);

        assertEquals(autoLockingTime, configuration.getWalletConfig().getAutoLockTime());
    }
}