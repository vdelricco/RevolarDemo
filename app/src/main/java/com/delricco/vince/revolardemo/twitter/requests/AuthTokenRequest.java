package com.delricco.vince.revolardemo.twitter.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class AuthTokenRequest extends StringRequest {
    private String encodedKeyAndSecret;

    public AuthTokenRequest(String url,
                             Response.Listener<String> listener,
                             Response.ErrorListener errorListener,
                             String encodedKeyAndSecret)
    {
        super(Request.Method.POST, url, listener, errorListener);
        this.encodedKeyAndSecret = encodedKeyAndSecret;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", String.valueOf(getBody().length));
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Authorization", "Basic " + encodedKeyAndSecret);
        return headers;
    }

    @Override
    public byte[] getBody() {
        return ("grant_type=client_credentials").getBytes();
    }
}