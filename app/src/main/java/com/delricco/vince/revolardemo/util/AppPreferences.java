package com.delricco.vince.revolardemo.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.delricco.vince.revolardemo.contacts.RevolarContact;
import com.google.gson.Gson;

import java.util.ArrayList;

public class AppPreferences {
    private static final String KEY_PREFS_CONTACT = "contact";
    private static final String KEY_PREFS_CONTACTS_SAVED = "contacts_saved";
    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName();
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    public AppPreferences(Context context) {
        sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        prefsEditor = sharedPrefs.edit();
    }

    public ArrayList<RevolarContact> getContacts() {
        ArrayList<RevolarContact> contacts = new ArrayList<>();
        for (int i = 0; i < getNumContacts(); i++) {
            String jsonContact = sharedPrefs.getString(KEY_PREFS_CONTACT + i, "");
            contacts.add(new Gson().fromJson(jsonContact, RevolarContact.class));
        }

        return contacts;
    }

    public void saveContacts(ArrayList<RevolarContact> contactList) {
        for (int i = 0; i < contactList.size(); i++) {
            prefsEditor.putString(KEY_PREFS_CONTACT + i, new Gson().toJson(contactList.get(i))).commit();
        }

        saveNumContacts(contactList.size());
    }

    public void clearContacts() {
        for (int i = 0; i < getNumContacts(); i++) {
            prefsEditor.remove(KEY_PREFS_CONTACT + i).commit();
        }

        saveNumContacts(0);
    }

    private void saveNumContacts(int numContacts) {
        prefsEditor.putInt(KEY_PREFS_CONTACTS_SAVED, numContacts).commit();
    }

    public int getNumContacts() {
        return sharedPrefs.getInt(KEY_PREFS_CONTACTS_SAVED, 0);
    }
}
