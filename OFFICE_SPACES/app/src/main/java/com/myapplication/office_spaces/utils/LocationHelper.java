package com.myapplication.office_spaces.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;
import java.util.Locale;

/**
 * Reusable helper that wraps runtime location-permission requests, the Fused
 * Location Provider, and reverse-geocoding into one small API so any Activity
 * (Renter home, Owner listing screen, Property details, Search, etc.) can get
 * "the user's current city + coordinates" with a couple of calls instead of
 * repeating this boilerplate everywhere.
 *
 * Must be created in onCreate() (before onStart) of an AppCompatActivity, since
 * it registers an ActivityResultLauncher.
 */
public class LocationHelper {

    public interface LocationCallback {
        /** Called with the device's current location once resolved. */
        void onLocationResult(Location location, String city, String fullAddress);

        /** Called if permission was denied or location could not be determined. */
        void onLocationUnavailable(String reason);
    }

    private final AppCompatActivity activity;
    private final SessionManager sessionManager;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<String[]> permissionLauncher;
    private LocationCallback pendingCallback;

    public LocationHelper(AppCompatActivity activity) {
        this.activity = activity;
        this.sessionManager = new SessionManager(activity);
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        this.permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    sessionManager.setAskedLocationPermission(true);
                    Boolean fine = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarse = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if ((fine != null && fine) || (coarse != null && coarse)) {
                        fetchLocation(pendingCallback);
                    } else if (pendingCallback != null) {
                        pendingCallback.onLocationUnavailable("Location permission denied");
                    }
                });
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permission if needed (first-time login prompt for both
     * Renter/Buyer and Owner roles), then resolves the current location.
     */
    public void requestPermissionAndFetch(LocationCallback callback) {
        sessionManager.setAskedLocationPermission(true);
        if (hasLocationPermission()) {
            fetchLocation(callback);
        } else {
            pendingCallback = callback;
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /** Fetches the current GPS location (assumes permission has already been granted). */
    public void fetchLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            if (callback != null) callback.onLocationUnavailable("Location permission not granted");
            return;
        }

        LocationManager lm = (LocationManager) activity.getSystemService(Activity.LOCATION_SERVICE);
        boolean gpsEnabled = lm != null && (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        if (!gpsEnabled) {
            if (callback != null) callback.onLocationUnavailable("Please enable location services (GPS)");
            return;
        }

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener(activity, location -> {
                        if (location == null) {
                            // Fall back to last known location if a fresh fix isn't available yet.
                            fusedLocationClient.getLastLocation().addOnSuccessListener(activity, last -> {
                                if (last != null) {
                                    resolveAndReturn(last, callback);
                                } else if (callback != null) {
                                    callback.onLocationUnavailable("Location not found");
                                }
                            });
                        } else {
                            resolveAndReturn(location, callback);
                        }
                    })
                    .addOnFailureListener(activity, e -> {
                        if (callback != null) callback.onLocationUnavailable("Couldn't fetch location");
                    });
        } catch (SecurityException e) {
            if (callback != null) callback.onLocationUnavailable("Location permission error");
        }
    }

    private void resolveAndReturn(Location location, LocationCallback callback) {
        String city = null;
        String fullAddress = null;
        try {
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
            @SuppressWarnings("deprecation")
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address a = addresses.get(0);
                city = a.getLocality();
                fullAddress = a.getAddressLine(0);
            }
        } catch (Exception ignored) {
            // Geocoder can throw if the network/geocoding backend is unavailable; we still
            // have raw coordinates, so continue without a city name.
        }

        sessionManager.saveLastLocation(location.getLatitude(), location.getLongitude(), city);

        if (callback != null) {
            callback.onLocationResult(location, city, fullAddress);
        }
    }

    /** Forward-geocodes a free-text address into lat/lng, off the UI thread's caller responsibility. */
    public double[] geocodeAddress(String fullAddress) {
        try {
            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
            @SuppressWarnings("deprecation")
            List<Address> results = geocoder.getFromLocationName(fullAddress, 1);
            if (results != null && !results.isEmpty()) {
                return new double[]{results.get(0).getLatitude(), results.get(0).getLongitude()};
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
