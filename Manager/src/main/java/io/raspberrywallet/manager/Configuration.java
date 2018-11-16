package io.raspberrywallet.manager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.raspberrywallet.contract.ServerConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static io.raspberrywallet.manager.Utils.println;

/**
 * Global configuration
 */
@Getter
@Setter(AccessLevel.PRIVATE)
final public class Configuration {

    /**
     * Base path to all wallet specific files, by default equals to $HOME
     */
    @JsonProperty("base-path-prefix")
    private String basePathPrefix = "/opt/wallet";

    /**
     * Current version
     */
    private String version = Configuration.class.getPackage().getImplementationVersion();

    /**
     * AutoLock idle time in seconds
     */
    @JsonProperty("autolock-seconds")
    private int autoLockSeconds = 60 * 5;

    /**
     * Modules configuration object, hides HashMap<String, JsonNode>
     * TODO unwrap from ModulesConfiguration and provide custom accessors
     */
    @JsonProperty("modules")
    private ModulesConfiguration modulesConfig;

    /**
     * Bitcoin configuration object
     */
    @JsonProperty("bitcoin")
    private BitcoinConfig bitcoinConfig;

    /**
     * Bitcoin configuration object
     */
    @JsonProperty("server")
    private ServerConfig serverConfig;

    public Configuration() {
        this(new ModulesConfiguration(), new BitcoinConfig());
    }

    public Configuration(ModulesConfiguration modulesConfig) {
        this(modulesConfig, new BitcoinConfig());
    }

    public Configuration(BitcoinConfig bitcoinConfig) {
        this(new ModulesConfiguration(), bitcoinConfig);
    }

    public Configuration(ModulesConfiguration modulesConfig, BitcoinConfig bitcoinConfig) {
        this.modulesConfig = modulesConfig;
        this.bitcoinConfig = bitcoinConfig;
        println(ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    public Configuration(ModulesConfiguration modulesConfig, BitcoinConfig bitcoinConfig,
                         String basePathPrefix) {
        this(modulesConfig, bitcoinConfig);
        this.basePathPrefix = basePathPrefix;
        println(ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    public Configuration(String basePathPrefix) {
        this();
        this.basePathPrefix = basePathPrefix;
        println(ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    /**
     * @param yamlFile configuration file encoded in YAML structure
     * @return YAML config parsed into Configuration object
     */
    static Configuration fromYamlFile(File yamlFile) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        Configuration config;
        try {
            config = objectMapper.readValue(yamlFile, Configuration.class);
        } catch (IOException e) {
            e.printStackTrace();
            config = new Configuration();
        }
        println(ReflectionToStringBuilder.toString(config, ToStringStyle.MULTI_LINE_STYLE));
        return config;
    }

    static Configuration testConfiguration() {
        return new Configuration("/tmp/wallet");
    }

    @NoArgsConstructor
    public static class ModulesConfiguration extends HashMap<String, JsonNode> {
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class BitcoinConfig {
        @JsonProperty("network")
        private String networkName = "testnet";
        @JsonProperty("user-agent")
        private String userAgent = "RaspberryWallet";
    }

}
