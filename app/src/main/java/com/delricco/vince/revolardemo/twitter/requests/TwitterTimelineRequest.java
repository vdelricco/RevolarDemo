package com.delricco.vince.revolardemo.twitter.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.delricco.vince.revolardemo.twitter.Authenticated;

import java.util.HashMap;
import java.util.Map;

public class TwitterTimelineRequest extends StringRequest {
    private Authenticated auth;

    public TwitterTimelineRequest(String url,
                                  Response.Listener<String> listener,
                                  Response.ErrorListener errorListener,
                                  Authenticated auth) {
        super(Method.GET, url, listener, errorListener);
        this.auth = auth;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + auth.getAccessToken());
        headers.put("Content-Type", "application/json");
        return headers;
    }
}