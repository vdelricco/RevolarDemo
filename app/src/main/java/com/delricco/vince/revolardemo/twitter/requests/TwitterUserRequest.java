package com.delricco.vince.revolardemo.twitter.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.delricco.vince.revolardemo.twitter.Authenticated;

import java.util.HashMap;
import java.util.Map;

public class TwitterUserRequest extends StringRequest {
    private Authenticated authenticated;

    public TwitterUserRequest(String url,
                               Response.Listener<String> listener,
                               Response.ErrorListener errorListener,
                               Authenticated authenticated)
    {
        super(Request.Method.GET, url, listener, errorListener);
        this.authenticated = authenticated;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + authenticated.access_token);
        headers.put("Content-Type", "application/json");
        return headers;
    }
}