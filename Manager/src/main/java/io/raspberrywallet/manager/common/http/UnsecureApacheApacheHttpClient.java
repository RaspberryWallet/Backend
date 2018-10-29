package io.raspberrywallet.manager.common.http;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnsecureApacheApacheHttpClient extends ApacheHttpClient {
    
    private Header[] defaultHeaders;
    
    public UnsecureApacheApacheHttpClient(Form defaultHeaders) {
        this.defaultHeaders = toHeadersArray(defaultHeaders);
    }
    
    @Override
    public HttpResponse sendPOSTRequest(Form body, String endpoint) throws IOException {
        org.apache.http.client.HttpClient client = HttpClients.createDefault();
        
        JSONObject jsonObject = convert(body);
        StringEntity stringEntity = new StringEntity(jsonObject.toString());
        
        HttpPost httpRequest = new HttpPost(endpoint);
        httpRequest.setEntity(stringEntity);
        httpRequest.setHeaders(defaultHeaders);
        
        return client.execute(httpRequest);
    }
    
    @Override
    public HttpResponse sendGETRequest(Form body, String endpoint) {
        throw new NotImplementedException();
    }
    
    private Header[] toHeadersArray(Form form) {
        List<NameValuePair> list = form.build();
        List<Header> result = new ArrayList<>(list.size());
        list.forEach(nameValuePair -> {
            BasicHeader basicHeader = new BasicHeader(nameValuePair.getName(), nameValuePair.getValue());
            result.add(basicHeader);
        });
        
        Header[] headers = new Header[result.size()];
        return result.toArray(headers);
    }
}
