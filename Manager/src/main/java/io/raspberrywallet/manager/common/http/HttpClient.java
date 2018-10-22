package io.raspberrywallet.manager.common.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public abstract class HttpClient {

    public abstract HttpResponse sendPOSTRequest(Form body, String endpoint) throws IOException;
    
    public abstract HttpResponse sendGETRequest(Form body, String endpoint);
    
    JSONObject convert(Form body) {
        List<NameValuePair> keyValuesList = body.build();
        JSONObject convertedJson = new JSONObject();
        keyValuesList.forEach(pair -> convertedJson.put(pair.getName(), pair.getValue()));
        return convertedJson;
    }
    
}
