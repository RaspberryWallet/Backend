package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.manager.common.wrappers.Credentials;
import io.raspberrywallet.manager.common.wrappers.Secret;
import io.raspberrywallet.manager.common.wrappers.Token;
import io.raspberrywallet.manager.common.http.UnsecureApacheApacheHttpClient;
import io.raspberrywallet.manager.common.http.ApacheHttpClient;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

class AuthorizationServerAPI {
    
    private AuthorizationServerConf configuration;

    private ApacheHttpClient httpClient;
    
    private Token token;
    private Boolean isRegisteredFlag, isLoggedInFlag;
    
    AuthorizationServerAPI(AuthorizationServerConf configuration) {
        this.configuration = configuration;
        Form defaultHeaders = Form.form()
                .add(HttpHeaders.CONTENT_TYPE, "application/json")
                .add("charset", "UTF-8");
    
        httpClient = new UnsecureApacheApacheHttpClient(defaultHeaders);
    }
    
    void login(Credentials credentials, int sessionLength) throws RequestException {
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.PASSWORD.val, credentials.getPasswordBase64())
                .add(APIKeys.SESSION_LENGTH.val, Integer.toString(sessionLength));
    
        login(requestBody, sessionLength);
    }
    
    void login(Credentials credentials) throws RequestException {
        login(credentials, 1800);
    }
    
    private void login(Form requestBody, int sessionLength) throws RequestException {
        try {
            HttpResponse httpResponse = executeRequest(requestBody, configuration.getLoginEndpoint());
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK)
                throw new RequestException("Request failed with error code: " + statusCode);
            
            String tokenString = EntityUtils.toString(httpResponse.getEntity());
            token = new Token(tokenString, sessionLength);
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }
    
    boolean logout(Credentials credentials) throws RequestException {
        Form body = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.TOKEN.val, token.getData());
        
        HttpResponse response = executeRequest(body, configuration.getLogoutEndpoint());
        isLoggedInFlag = !handleResponse(response);
        return isLoggedInFlag;
    }
    
    boolean register(Credentials credentials) throws RequestException {
        Form body = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.PASSWORD.val, credentials.getPasswordBase64());
        
        HttpResponse response = executeRequest(body, configuration.getRegisterEndpoint());
        return handleResponse(response);
    }
    
    boolean isRegistered(Credentials credentials) throws RequestException {
        if (isRegisteredFlag != null)
            return isRegisteredFlag;
        
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName());
        
        HttpResponse response = executeRequest(requestBody, configuration.getWalletExistsEndpoint());
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            isRegisteredFlag = true;
            return true;
        }
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            isRegisteredFlag = false;
            return false;
        } else
            throw new RequestException("Request failed with error code: " + statusCode);
    }
    
    /**
     *
     * @return Base64 encoded secret
     * @throws RequestException
     */
    String getSecret(Credentials credentials) throws RequestException {
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.TOKEN.val, token.getData());
        
        try {
            HttpResponse httpResponse = executeRequest(requestBody, configuration.getGetSecretEndpoint());
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }
    
    /**
     * Since this method is not needed right now, it will not be implemented.
     * @param secret empty
     * @return  empty
     */
    boolean setSecret(Secret secret) {
        throw new NotImplementedException();
    }
    
    void overwriteSecret(Credentials credentials, String secret) throws RequestException {
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.TOKEN.val, token.getData())
                .add(APIKeys.SECRET.val, secret);
        
        HttpResponse response = executeRequest(requestBody, configuration.getOverwriteEndpoint());
        handleResponse(response);
    }
    
    boolean secretIsSet(Credentials credentials) throws RequestException {
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.TOKEN.val, token.getData());
        
        HttpResponse response = executeRequest(requestBody, configuration.getIsSecretSetEndpoint());
        
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
            return true;
        if (statusCode == HttpStatus.SC_NOT_FOUND)
            return false;
        else
            throw new RequestException("Request failed with error code: " + statusCode);
    }
    
    private HttpResponse executeRequest(Form body, String endpoint) throws RequestException {
        try {
            return httpClient.sendPOSTRequest(body, endpoint);
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }
    
    private boolean handleResponse(HttpResponse response) throws RequestException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK)
            throw new RequestException("Request failed with error code: " + statusCode);
        
        return true;
    }
    
    public void registerAndLogin(Credentials credentials) throws RequestException {
        if (!isRegisteredCheck(credentials))
            isRegisteredFlag = register(credentials);
        
        if (!isLoggedIn()) {
            login(credentials);
            isLoggedInFlag = true;
        }
    }
    
    private boolean isRegisteredCheck(Credentials credentials) throws RequestException {
        if (isRegisteredFlag == null || !isRegisteredFlag)
            isRegisteredFlag = isRegistered(credentials);
        
        return isRegisteredFlag;
    }
    
    boolean isLoggedIn() {
        return token != null && !token.isExpired();
    }
    
}
