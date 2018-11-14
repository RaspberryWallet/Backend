package io.raspberrywallet.manager.common.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Form;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class SecureApacheHttpClient extends ApacheHttpClient {
    
    public SecureApacheHttpClient(Form defaultHeaders, boolean acceptAllCerts) {
        super(defaultHeaders);
        httpClient = acceptAllCerts ? setupAcceptAllHttpClient() : HttpClients.createDefault();
    }
    
    private HttpClient setupAcceptAllHttpClient() {
        SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder()
                    .loadTrustMaterial(null, (certificate, authType) -> true).build();
        
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException impossible) {
            throw new RuntimeException(impossible);
        }
    
        return HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
    }
}
