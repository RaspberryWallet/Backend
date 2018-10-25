package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.manager.common.wrappers.Credentials;
import io.raspberrywallet.manager.common.wrappers.Secret;
import io.raspberrywallet.manager.common.wrappers.Token;
import io.raspberrywallet.manager.common.http.ApacheHttpClient;
import io.raspberrywallet.manager.common.http.HttpClient;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.LocalDate;

class AuthorizationServerAPI {
    
    private AuthorizationServerConf configuration;
    private Credentials credentials;

    private HttpClient httpClient;
    private Form preparedCredentials;
    
    private Token token;
    private Boolean isRegisteredFlag, isLoggedInFlag;
    
    AuthorizationServerAPI(AuthorizationServerConf configuration, Credentials credentials) {
        this.configuration = configuration;
        Form defaultHeaders = Form.form()
                .add(HttpHeaders.CONTENT_TYPE, "application/json")
                .add("charset", "UTF-8");
    
        this.credentials = credentials;
        preparedCredentials = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.PASSWORD.val, credentials.getPassword());
    
        httpClient = new ApacheHttpClient(configuration.getHost(), defaultHeaders);
    }
    
    Token login(int sessionLength) throws RequestException {
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.PASSWORD.val, credentials.getPassword())
                .add(APIKeys.SESSION_LENGTH.val, Integer.toString(sessionLength));
    
        return login(requestBody);
    }
    
    Token login() throws RequestException {
        return login(preparedCredentials);
    }
    
    private Token login(Form requestBody) throws RequestException {
        try {
            HttpResponse httpResponse = executeRequest(requestBody, configuration.getLoginEndpoint());
            String token = EntityUtils.toString(httpResponse.getEntity());
            return new Token(token, LocalDate.MAX);
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }
    
    boolean logout() throws RequestException {
        HttpResponse response = executeRequest(preparedCredentials, configuration.getLogoutEndpoint());
        isLoggedInFlag = !handleResponse(response);
        return isLoggedInFlag;
    }
    
    boolean register() throws RequestException {
        HttpResponse response = executeRequest(preparedCredentials, configuration.getRegisterEndpoint());
        return handleResponse(response);
    }
    
    boolean isRegistered() throws RequestException {
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
    String getSecret() throws RequestException {
        registerAndLogin();
    
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
    
    void overwriteSecret(String secret) throws RequestException {
        registerAndLogin();
        
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.TOKEN.val, token.getData())
                .add(APIKeys.SECRET.val, secret);
        
        HttpResponse response = executeRequest(requestBody, configuration.getOverwriteEndpoint());
        handleResponse(response);
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
    
    private void registerAndLogin() throws RequestException {
        if (!isRegisteredCheck())
            isRegisteredFlag = register();
        
        if (!isLoggedIn()) {
            token = login();
            isLoggedInFlag = true;
        }
    }
    
    private boolean isRegisteredCheck() throws RequestException {
        if (isRegisteredFlag == null || !isRegisteredFlag)
            isRegisteredFlag = isRegistered();
        
        return isRegisteredFlag;
    }
    
    private boolean isLoggedIn() {
        if (token == null || token.isExpired())
            return false;
        else
            return true;
    }
    
}
