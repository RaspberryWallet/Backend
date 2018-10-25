package io.raspberrywallet.manager.modules.authorizationserver;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
class AuthorizationServerConf {
    
    AuthorizationServerConf() {}
    
    private String host = "http://localhost";
    private int port = 8080;
    
    private String address = host + ":" + port;
    
    private String loginEndpoint = address + "/authorization/login";
    private String logoutEndpoint = address + "/authorization/logout";
    private String registerEndpoint = address + "/authorization/register";
    private String setSecretEndpoint = address + "/authorization/secret/set";
    private String getSecretEndpoint = address + "/authorization/secret/get";
    private String overwriteEndpoint = address + "/authorization/secret/overwrite";
    private String walletExistsEndpoint = address + "/authorization/exists";
    private String isSecretSetEndpoint = address + "/authorization/secret/exists";
    
}
