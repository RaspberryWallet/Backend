package io.raspberrywallet.manager.common.http;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;

import java.io.IOException;

public class ApacheSecuredHttpClient extends HttpClient {
    
    @Override
    public HttpResponse sendPOSTRequest(Form body, String endpoint) throws IOException {
        throw new NotImplementedException();
    }
    
    @Override
    public HttpResponse sendGETRequest(Form body, String endpoint) {
        throw new NotImplementedException();
    }
}
