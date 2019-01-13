package io.raspberrywallet.manager.modules.authorizationserver;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.raspberrywallet.manager.modules.ModuleConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorizationServerConfig implements ModuleConfig {
    private String host = "https://authorization-server";
    private int port = 8443;
    private String address = host + ":" + port;
    private Endpoints endpoints = new Endpoints();
    
    /**
     * By default true, needed for debugging. User will overwrite it
     * with hist own configuration yaml file anyways.
     */
    @JsonProperty("accept-untrusted-certs")
    private boolean acceptUntrustedCerts = true;
    
    boolean getAcceptUntrustedCerts() {
        return acceptUntrustedCerts;
    }

    String getLoginEndpoint() {
        return address + endpoints.getLogin();
    }

    String getLogoutEndpoint() {
        return address + endpoints.getLogout();
    }

    String getRegisterEndpoint() {
        return address + endpoints.getRegister();
    }

    String getSetSecretEndpoint() {
        return address + endpoints.getSetSecret();
    }

    String getGetSecretEndpoint() {
        return address + endpoints.getGetSecret();
    }

    String getOverwriteEndpoint() {
        return address + endpoints.getOverwrite();
    }

    String getWalletExistsEndpoint() {
        return address + endpoints.getWalletExists();
    }

    String getIsSecretSetEndpoint() {
        return address + endpoints.getIsSecretSet();
    }

    @Getter
    @NoArgsConstructor
    public static class Endpoints {
        private String login = "/authorization/login";
        private String logout = "/authorization/logout";
        private String register = "/authorization/register";
        @JsonProperty("set-secret")
        private String setSecret = "/authorization/secret/set";
        @JsonProperty("get-secret")
        private String getSecret = "/authorization/secret/get";
        @JsonProperty("overwrite")
        private String overwrite = "/authorization/secret/overwrite";
        @JsonProperty("wallet-exists")
        private String walletExists = "/authorization/exists";
        @JsonProperty("is-secret-set")
        private String isSecretSet = "/authorization/secret/exists";
    }
}
