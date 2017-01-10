package com.delricco.vince.revolardemo;

import com.delricco.vince.revolardemo.contacts.RevolarContact;
import com.delricco.vince.revolardemo.twitter.Authenticated;
import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GsonUnitTest {
    @Test
    public void gsonToRevolarContact() throws Exception {
        Gson gson = new Gson();
        RevolarContact contact = new RevolarContact("Test", "1234567890");
        String jsonContact = gson.toJson(contact);
        RevolarContact gsonContact = gson.fromJson(jsonContact, RevolarContact.class);

        assertEquals(contact.getName(), gsonContact.getName());
        assertEquals(contact.getNumber(), gsonContact.getNumber());
    }

    @Test
    public void gsonToAuthenticated() throws Exception {
        Gson gson = new Gson();
        Authenticated auth = new Authenticated("test token", "test_type");
        String jsonAuth = gson.toJson(auth);
        Authenticated gsonAuth = gson.fromJson(jsonAuth, Authenticated.class);

        assertEquals(auth.getAccessToken(), gsonAuth.getAccessToken());
        assertEquals(auth.getTokenType(), gsonAuth.getTokenType());
    }
}
