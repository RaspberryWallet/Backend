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

@Getter
@Setter(AccessLevel.PRIVATE)
final public class Configuration {

    @JsonProperty("session-length")
    private long sessionLength = 3600000;

    @JsonProperty("base-path-prefix")
    private String basePathPrefix = "/opt/wallet";

    private String version = Configuration.class.getPackage().getImplementationVersion();

    @JsonProperty("modules")
    private ModulesConfiguration modulesConfig;

    @JsonProperty("bitcoin")
    private BitcoinConfiguration bitcoinConfig;

    public Configuration() {
        this(new ModulesConfiguration(), new BitcoinConfiguration());
    }

    public Configuration(ModulesConfiguration modulesConfig) {
        this(modulesConfig, new BitcoinConfiguration());
    }

    public Configuration(BitcoinConfiguration bitcoinConfig) {
        this(new ModulesConfiguration(), bitcoinConfig);
    }

    public Configuration(ModulesConfiguration modulesConfig, BitcoinConfiguration bitcoinConfig) {
        this.modulesConfig = modulesConfig;
        this.bitcoinConfig = bitcoinConfig;
        println(ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    public Configuration(ModulesConfiguration modulesConfig, BitcoinConfiguration bitcoinConfig, long sessionLength,
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
    static class BitcoinConfiguration {
        @JsonProperty("autolock-time")
        private long autoLockTime;
        @JsonProperty("wallet-filepath")
        public String walletFilePath;
        @JsonProperty("blockchain-filepath")
        private String blockChainFilePath;
    }
}
