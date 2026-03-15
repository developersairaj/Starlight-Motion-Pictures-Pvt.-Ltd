package com.example.starlight.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME     = "starlight_session";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL     = "user_email";
    private static final String KEY_NAME      = "user_name";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs  = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String name, String email) {
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_NAME, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}