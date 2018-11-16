package io.raspberrywallet.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Setter
@Getter
public class ServerConfig {
    @JsonProperty("keystore-name")
    public String keystoreName = "RaspberryWallet.keystore";
    @JsonProperty("keystore-password")
    public char[] keystorePassword = "raspberrywallet".toCharArray();
    @JsonProperty("key-alias")
    public String keyAlias = "ssl";
    public int port = 9080;
    @JsonProperty("secure-port")
    public int securePort = 9433;
}
