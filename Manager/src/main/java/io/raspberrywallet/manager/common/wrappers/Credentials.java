package io.raspberrywallet.manager.common.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Base64;

@AllArgsConstructor
@Getter
public class Credentials {
    
    private String name;
    private String password;
    
    public String getPasswordBase64() {
        return Base64.getUrlEncoder().encodeToString(password.getBytes());
    }
    
}
