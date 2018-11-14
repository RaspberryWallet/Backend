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

public abstract class ApacheHttpClient {
    
    Header[] defaultHeaders;
    HttpClient httpClient;
    
    ApacheHttpClient(Form defaultHeaders) {
        this.defaultHeaders = toHeadersArray(defaultHeaders);
        httpClient = HttpClients.createDefault();
    }
    
    public HttpResponse sendPOSTRequest(Form body, String endpoint) throws IOException {
        JSONObject jsonObject = convert(body);
        StringEntity stringEntity = new StringEntity(jsonObject.toString());
    
        HttpPost httpRequest = new HttpPost(endpoint);
        httpRequest.setEntity(stringEntity);
        httpRequest.setHeaders(defaultHeaders);
    
        return httpClient.execute(httpRequest);
    }
    
    public HttpResponse sendGETRequest(Form body, String endpoint) {
        throw new NotImplementedException();
    }
    
    JSONObject convert(Form body) {
        List<NameValuePair> keyValuesList = body.build();
        JSONObject convertedJson = new JSONObject();
        keyValuesList.forEach(pair -> convertedJson.put(pair.getName(), pair.getValue()));
        return convertedJson;
    }
    
    Header[] toHeadersArray(Form form) {
        List<NameValuePair> list = form.build();
        return list.stream()
                .map(nameValuePair -> new BasicHeader(nameValuePair.getName(), nameValuePair.getValue()))
                .toArray(Header[]::new);
    }
    
}
