package com.delricco.vince.revolardemo.twitter;

public class Authenticated {
    private String accessToken;
    private String tokenType;

    public Authenticated(String token, String type) {
        this.accessToken = token;
        this.tokenType = type;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
