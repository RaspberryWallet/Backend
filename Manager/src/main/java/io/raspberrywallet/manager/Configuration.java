package io.raspberrywallet.manager;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
     * Session length in millis
     */
    @JsonProperty("session-length")
    private long sessionLength = 3600000;

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
     * AutoLock idle time in millis
     */
    @JsonProperty("autolock-time")
    private long autoLockTime = 10000;

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

    public Configuration(ModulesConfiguration modulesConfig, BitcoinConfig bitcoinConfig, long sessionLength,
                         String basePathPrefix, String version) {
        this(modulesConfig, bitcoinConfig);
        this.sessionLength = sessionLength;
        this.basePathPrefix = basePathPrefix;
        this.version = version;
        println(ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    public Configuration(long sessionLength, String basePathPrefix, String version) {
        this();
        this.sessionLength = sessionLength;
        this.basePathPrefix = basePathPrefix;
        this.version = version;
        println(ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    /**
     * @param yamlFile configuration file encoded in YAML structure
     * @return YAML config parsed into Configuration object
     */
    public static Configuration fromYamlFile(File yamlFile) {
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


    @NoArgsConstructor
    public static class ModulesConfiguration extends HashMap<String, JsonNode> {
    }

    @NoArgsConstructor
    @Setter
    @Getter
    public static class BitcoinConfig {
        @JsonProperty("network")
        public String networkName = "testnet";
    }
}
