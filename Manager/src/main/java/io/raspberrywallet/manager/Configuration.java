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

@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
final public class Configuration {

    @JsonProperty("session-length")
    private long sessionLength = 3600000;

    @JsonProperty("base-path-prefix")
    private String basePathPrefix = "/opt/wallet";

    private String version = Configuration.class.getPackage().getImplementationVersion();
    private ModulesConfiguration modules;
    private WalletConfiguration wallet;

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
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PUBLIC)
    static class WalletConfiguration {
        @JsonProperty("autolock-time")
        private int autoLockTime;
        @JsonProperty("wallet-filepath")
        public String walletFilePath;
        @JsonProperty("blockchain-filepath")
        private String blockChainFilePath;
    }
}
