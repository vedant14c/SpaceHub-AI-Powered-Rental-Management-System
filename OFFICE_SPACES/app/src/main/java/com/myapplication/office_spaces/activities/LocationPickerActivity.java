package com.myapplication.office_spaces.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.utils.LocationHelper;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.List;
import java.util.Locale;

/**
 * Lets an Owner drop a pin anywhere on a Google Map to select a property's exact
 * location, as an alternative to typing an address (and as a fallback when the
 * device's GPS/geocoder can't resolve an address automatically).
 *
 * Returns RESULT_OK with EXTRA_LATITUDE / EXTRA_LONGITUDE / EXTRA_ADDRESS /
 * EXTRA_CITY / EXTRA_STATE set on the result Intent.
 */
public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LATITUDE = "picked_latitude";
    public static final String EXTRA_LONGITUDE = "picked_longitude";
    public static final String EXTRA_ADDRESS = "picked_address";
    public static final String EXTRA_CITY = "picked_city";
    public static final String EXTRA_STATE = "picked_state";

    private static final LatLng DEFAULT_LATLNG = new LatLng(19.0760, 72.8777); // Mumbai fallback

    private GoogleMap map;
    private TextView txtSelectedAddress;
    private LocationHelper locationHelper;
    private double selectedLat, selectedLng;
    private String selectedAddress, selectedCity, selectedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        txtSelectedAddress = findViewById(R.id.txtSelectedAddress);
        locationHelper = new LocationHelper(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> confirmSelection());
        findViewById(R.id.fabMyLocation).setOnClickListener(v -> centerOnCurrentLocation());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng start = DEFAULT_LATLNG;
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.hasLastLocation()) {
            start = new LatLng(sessionManager.getLastLatitude(), sessionManager.getLastLongitude());
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15f));
        updateSelectedLocation(start);

        // The pin stays fixed in the centre of the screen (see layout); we just read off
        // the camera's target position whenever the user finishes panning the map.
        map.setOnCameraIdleListener(() -> updateSelectedLocation(map.getCameraPosition().target));

        try {
            map.setMyLocationEnabled(locationHelper.hasLocationPermission());
        } catch (SecurityException ignored) {
        }
    }

    private void centerOnCurrentLocation() {
        locationHelper.requestPermissionAndFetch(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(android.location.Location location, String city, String fullAddress) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (map != null) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
                }
            }

            @Override
            public void onLocationUnavailable(String reason) {
                Toast.makeText(LocationPickerActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSelectedLocation(LatLng latLng) {
        selectedLat = latLng.latitude;
        selectedLng = latLng.longitude;
        txtSelectedAddress.setText("Locating address…");

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            @SuppressWarnings("deprecation")
            List<Address> results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (results != null && !results.isEmpty()) {
                Address a = results.get(0);
                selectedAddress = a.getAddressLine(0);
                selectedCity = a.getLocality();
                selectedState = a.getAdminArea();
                txtSelectedAddress.setText(selectedAddress != null ? selectedAddress
                        : String.format(Locale.getDefault(), "%.5f, %.5f", selectedLat, selectedLng));
            } else {
                selectedAddress = null;
                txtSelectedAddress.setText(String.format(Locale.getDefault(), "%.5f, %.5f", selectedLat, selectedLng));
            }
        } catch (Exception e) {
            selectedAddress = null;
            txtSelectedAddress.setText(String.format(Locale.getDefault(), "%.5f, %.5f", selectedLat, selectedLng));
        }
    }

    private void confirmSelection() {
        Intent result = new Intent();
        result.putExtra(EXTRA_LATITUDE, selectedLat);
        result.putExtra(EXTRA_LONGITUDE, selectedLng);
        result.putExtra(EXTRA_ADDRESS, selectedAddress);
        result.putExtra(EXTRA_CITY, selectedCity);
        result.putExtra(EXTRA_STATE, selectedState);
        setResult(RESULT_OK, result);
        finish();
    }
}
