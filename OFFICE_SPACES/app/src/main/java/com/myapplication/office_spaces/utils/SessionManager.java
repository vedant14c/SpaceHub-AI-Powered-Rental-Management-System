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
    private static final String KEY_LAST_LAT = "lastLatitude";
    private static final String KEY_LAST_LNG = "lastLongitude";
    private static final String KEY_LAST_CITY = "lastCity";
    private static final String KEY_LOCATION_ASKED = "locationPermissionAsked";

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

    /**
     * Caches the user's last known GPS location + detected city so any screen
     * (Add Listing, Property Details, Search) can reuse it without re-requesting
     * permission or waiting on a fresh GPS fix.
     */
    public void saveLastLocation(double latitude, double longitude, String city) {
        prefs.edit()
                .putFloat(KEY_LAST_LAT, (float) latitude)
                .putFloat(KEY_LAST_LNG, (float) longitude)
                .putString(KEY_LAST_CITY, city)
                .apply();
    }

    public boolean hasLastLocation() {
        return prefs.contains(KEY_LAST_LAT) && prefs.contains(KEY_LAST_LNG);
    }

    public double getLastLatitude() { return prefs.getFloat(KEY_LAST_LAT, 0f); }

    public double getLastLongitude() { return prefs.getFloat(KEY_LAST_LNG, 0f); }

    public String getLastCity() { return prefs.getString(KEY_LAST_CITY, null); }

    /** Tracks whether we've already prompted this user for location permission once. */
    public boolean hasAskedLocationPermission() { return prefs.getBoolean(KEY_LOCATION_ASKED, false); }

    public void setAskedLocationPermission(boolean asked) {
        prefs.edit().putBoolean(KEY_LOCATION_ASKED, asked).apply();
    }
}