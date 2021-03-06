package io.raspberrywallet.manager.modules.authorizationserver;

import io.raspberrywallet.manager.common.http.ApacheHttpClient;
import io.raspberrywallet.manager.common.http.SecureApacheHttpClient;
import io.raspberrywallet.manager.common.http.UnsecureApacheHttpClient;
import io.raspberrywallet.manager.common.wrappers.Credentials;
import io.raspberrywallet.manager.common.wrappers.Secret;
import io.raspberrywallet.manager.common.wrappers.Token;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

class AuthorizationServerAPI {

    private AuthorizationServerConfig configuration;

    private ApacheHttpClient httpClient;

    private Token token;

    AuthorizationServerAPI(@NotNull AuthorizationServerConfig configuration) {
        this.configuration = configuration;
        Form defaultHeaders = Form.form()
                .add(HttpHeaders.CONTENT_TYPE, "application/json")
                .add("charset", "UTF-8");

        if (configuration.getAddress().startsWith("http://"))
            httpClient = new UnsecureApacheHttpClient(defaultHeaders);
        else
            httpClient = new SecureApacheHttpClient(defaultHeaders, configuration.getAcceptUntrustedCerts());
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
        return !handleResponse(response);
    }

    boolean register(Credentials credentials) throws RequestException {
        Form body = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName())
                .add(APIKeys.PASSWORD.val, credentials.getPasswordBase64());

        HttpResponse response = executeRequest(body, configuration.getRegisterEndpoint());
        return handleResponse(response);
    }

    boolean isRegistered(Credentials credentials) throws RequestException {
        Form requestBody = Form.form()
                .add(APIKeys.WALLETUUID.val, credentials.getName());

        HttpResponse response = executeRequest(requestBody, configuration.getWalletExistsEndpoint());
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK) {
            return true;
        }
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
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

    boolean isLoggedIn() {
        return token != null && !token.isExpired();
    }

}
