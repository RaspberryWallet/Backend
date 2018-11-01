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

    @JsonProperty("wallet")
    private WalletConfiguration walletConfig;

    public Configuration() {
        this(new ModulesConfiguration(), new WalletConfiguration());
    }

    public Configuration(ModulesConfiguration modulesConfig) {
        this(modulesConfig, new WalletConfiguration());
    }

    public Configuration(WalletConfiguration walletConfig) {
        this(new ModulesConfiguration(), walletConfig);
    }

    public Configuration(ModulesConfiguration modulesConfig, WalletConfiguration walletConfig) {
        this.modulesConfig = modulesConfig;
        this.walletConfig = walletConfig;
        println(ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE));
    }

    public Configuration(ModulesConfiguration modulesConfig, WalletConfiguration walletConfig, long sessionLength,
                         String basePathPrefix, String version) {
        this(modulesConfig, walletConfig);
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
    static class WalletConfiguration {
        @JsonProperty("autolock-time")
        private long autoLockTime;
        @JsonProperty("wallet-filepath")
        public String walletFilePath;
        @JsonProperty("blockchain-filepath")
        private String blockChainFilePath;
    }
}
