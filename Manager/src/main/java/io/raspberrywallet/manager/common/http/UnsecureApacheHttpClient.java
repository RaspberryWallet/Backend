package io.raspberrywallet.manager.common.http;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnsecureApacheHttpClient extends ApacheHttpClient {
    
    private HttpClient client = HttpClients.createDefault();
    
    public UnsecureApacheHttpClient(Form defaultHeaders) {
        super(defaultHeaders);
    }
    
}
