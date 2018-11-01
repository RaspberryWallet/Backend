package io.raspberrywallet.manager;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationTest {

    @Test
    void fromYamlFile() {
        URL configUrl = getClass().getClassLoader().getResource("files/exampleConfig.yaml");
        File exampleConfigFile = new File(configUrl.getPath());
        Configuration configuration = Configuration.fromYamlFile(exampleConfigFile);
        
        assertEquals(configuration.getVersion(), "0.5.0");
        assertEquals(configuration.getSessionLength(), 3600000);
        assertEquals(configuration.getBasePathPrefix(), "/opt/wallet/");
        assertEquals(configuration.getModulesConfig().size(), 4);
        assertNotNull(configuration.getBitcoinConfig());
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

}