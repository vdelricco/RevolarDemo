package com.delricco.vince.revolardemo.twitter;

public class Authenticated {

    private String access_token;
    private String token_type;

    public Authenticated(String token, String type) {
        this.access_token = token;
        this.token_type = type;
    }

    public String getAccessToken() {
        return access_token;
    }

    public String getTokenType() {
        return token_type;
    }
}
