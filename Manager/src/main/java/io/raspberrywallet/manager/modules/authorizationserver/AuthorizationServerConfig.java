package io.raspberrywallet.manager.modules.authorizationserver;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.raspberrywallet.manager.modules.ModuleConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Getter
@NoArgsConstructor
class AuthorizationServerConfig implements ModuleConfig {

    private String host = "http://localhost";
    private int port = 8080;
    private String address = host + ":" + port;
    private boolean https = false;
    private Endpoints endpoints = new Endpoints();


    @Getter
    @NoArgsConstructor
    public class Endpoints extends HashMap<String, String> {
        private String login = address + "/authorization/login";
        private String logout = address + "/authorization/logout";
        private String register = address + "/authorization/register";
        @JsonProperty("set-secret")
        private String setSecret = address + "/authorization/secret/set";
        @JsonProperty("get-secret")
        private String getSecret = address + "/authorization/secret/get";
        @JsonProperty("overwrite")
        private String overwrite = address + "/authorization/secret/overwrite";
        @JsonProperty("wallet-exists")
        private String walletExists = address + "/authorization/exists";
        @JsonProperty("is-secret-set")
        private String isSecretSet = address + "/authorization/secret/exists";
    }
}
