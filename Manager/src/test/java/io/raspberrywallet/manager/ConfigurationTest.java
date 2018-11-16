package io.raspberrywallet.manager;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigurationTest {

    @Test
    void fromYamlFile() throws IOException {
        String version = "0.5.0";
        String basePrefixDir = "/opt/wallet";
        String configYamlContent = "" +
                "version: " + version + "\n" +
                "base-path-prefix: " + basePrefixDir + "\n" +
                "autolock-seconds: 1800\n" +
                "bitcoin:\n" +
                "  network: testnet\n" +
                "  user-agent: RaspberryWallet\n" +
                "\n" +
                "  AuthorizationServerModule:\n" +
                "    host: 89.89.89.89\n" +
                "    port: 8080\n" +
                "    endpoints:\n" +
                "      set-secret: /authorization/secret/set\n" +
                "      overwritte-secret: /authorization/secret/overwritte\n" +
                "\n" +
                "  PushButtonModule:\n" +
                "    some: configuration\n" +
                "\n" +
                "  ExampleModule:\n" +
                "    example: example\n" +
                "    name: name\n";
        File tmpConfig = new File("tempConfig.yaml");
        try {
            Files.write(tmpConfig.toPath(), configYamlContent.getBytes());
            Configuration configuration = Configuration.fromYamlFile(tmpConfig);
            assertEquals(configuration.getVersion(), version);
            assertEquals(configuration.getBasePathPrefix(), basePrefixDir);
            assertEquals(configuration.getModulesConfig().size(), 4);
            assertNotNull(configuration.getBitcoinConfig());
        } finally {
            tmpConfig.delete();
        }
    }

    @Test
    void getBasePathPrefix() {
        final String basePathPrefix = "/opt/wallet";
        Configuration configuration = new Configuration(basePathPrefix);
        assertEquals(basePathPrefix, configuration.getBasePathPrefix());
    }
    
}