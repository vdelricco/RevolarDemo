package com.delricco.vince.revolardemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

public class AppPreferences {
    public static final String KEY_PREFS_CONTACT = "contact";
    public static final String KEY_PREFS_CONTACTS_SAVED = "contacts_saved";
    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName();
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private Gson gson;

    public AppPreferences(Context context) {
        sharedPrefs = context.getSharedPreferences("AppPreferences", Activity.MODE_PRIVATE);
        prefsEditor = sharedPrefs.edit();
        gson = new Gson();
    }

    public ArrayList<RevolarContact> getContacts() {
        ArrayList<RevolarContact> contacts = new ArrayList<>();
        for (int i = 0; i < getNumContacts(); i++) {
            String jsonContact = sharedPrefs.getString(KEY_PREFS_CONTACT + i, "");
            contacts.add(gson.fromJson(jsonContact, RevolarContact.class));
        }

        return contacts;
    }

    public void saveContacts(ArrayList<RevolarContact> contactList) {
        for (int i = 0; i < contactList.size(); i++) {
            prefsEditor.putString(KEY_PREFS_CONTACT + i, gson.toJson(contactList.get(i))).commit();
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

    private int getNumContacts() {
        return sharedPrefs.getInt(KEY_PREFS_CONTACTS_SAVED, 0);
    }
}
