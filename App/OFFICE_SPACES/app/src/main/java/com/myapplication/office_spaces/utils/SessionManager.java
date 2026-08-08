package com.myapplication.office_spaces.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "office_spaces_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";
    private static final String KEY_FAVORITES = "favorites";
    
    // Preferences
    private static final String KEY_PREF_CITY = "pref_city";
    private static final String KEY_PREF_PROP_TYPE = "pref_prop_type";
    private static final String KEY_PREF_LIST_TYPE = "pref_list_type";
    private static final String KEY_PREF_BUDGET = "pref_budget";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(int userId, String name, String email, String role, String token) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public String getToken()  { return prefs.getString(KEY_TOKEN, null); }
    public String getName()   { return prefs.getString(KEY_NAME, null); }
    public String getEmail()  { return prefs.getString(KEY_EMAIL, null); }
    public String getRole()   { return prefs.getString(KEY_ROLE, null); }
    public int getUserId()    { return prefs.getInt(KEY_USER_ID, -1); }
    public boolean isLoggedIn() { return getToken() != null; }
    public void clearSession() { prefs.edit().clear().apply(); }

    public void savePreferences(String city, String propType, String listType, Double budget) {
        prefs.edit()
                .putString(KEY_PREF_CITY, city)
                .putString(KEY_PREF_PROP_TYPE, propType)
                .putString(KEY_PREF_LIST_TYPE, listType)
                .putString(KEY_PREF_BUDGET, budget != null ? String.valueOf(budget) : null)
                .apply();
    }

    public String getPreferredCity() { return prefs.getString(KEY_PREF_CITY, ""); }
    public String getPreferredPropertyType() { return prefs.getString(KEY_PREF_PROP_TYPE, ""); }
    public String getPreferredListingType() { return prefs.getString(KEY_PREF_LIST_TYPE, ""); }
    public Double getMaxBudget() { 
        String b = prefs.getString(KEY_PREF_BUDGET, null);
        return (b != null) ? Double.valueOf(b) : 0.0;
    }

    public void setFavorite(int propertyId, boolean isFavorite) {
        java.util.Set<String> favorites = new java.util.HashSet<>(
                prefs.getStringSet(KEY_FAVORITES, new java.util.HashSet<>())
        );
        if (isFavorite) {
            favorites.add(String.valueOf(propertyId));
        } else {
            favorites.remove(String.valueOf(propertyId));
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    public boolean isFavorite(int propertyId) {
        java.util.Set<String> favorites = prefs.getStringSet(KEY_FAVORITES, new java.util.HashSet<>());
        return favorites.contains(String.valueOf(propertyId));
    }
}