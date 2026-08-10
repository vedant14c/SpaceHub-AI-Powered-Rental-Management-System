package com.myapplication.office_spaces.activities;


import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.fragments.OwnerDashboardFragment;
import com.myapplication.office_spaces.fragments.OwnerInquiriesFragment;
import com.myapplication.office_spaces.fragments.OwnerListingsFragment;
import com.myapplication.office_spaces.fragments.OwnerProfileFragment;
import com.myapplication.office_spaces.utils.LocationHelper;
import com.myapplication.office_spaces.utils.SessionManager;

public class OwnerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);

        bottomNav = findViewById(R.id.ownerBottomNav);
        locationHelper = new LocationHelper(this);

        if (savedInstanceState == null) {
            loadFragment(new OwnerDashboardFragment());
        }

        requestLocationOnFirstLogin();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_owner_home) {
                loadFragment(new OwnerDashboardFragment());
                return true;
            } else if (id == R.id.nav_owner_listings) {
                loadFragment(new OwnerListingsFragment());
                return true;
            } else if (id == R.id.nav_owner_inquiries) {
                loadFragment(new OwnerInquiriesFragment());
                return true;
            } else if (id == R.id.nav_owner_profile) {
                loadFragment(new OwnerProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.ownerFragmentContainer, fragment)
                .commit();
    }

    /** Called from OwnerDashboardFragment's Quick Actions to jump to another tab. */
    public void switchTab(int menuItemId) {
        bottomNav.setSelectedItemId(menuItemId);
    }

    /**
     * Requests location permission the first time an Owner opens the app (so it's ready
     * to prefill "current location" on the Add Listing screen), and caches the result.
     * If the Owner denies it, they can still grant it later from Settings or use the
     * "Pick on Map" option instead when adding a listing.
     */
    private void requestLocationOnFirstLogin() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.hasAskedLocationPermission()) return;

        locationHelper.requestPermissionAndFetch(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(android.location.Location location, String city, String fullAddress) {
                // Cached silently via SessionManager; Add Listing screen will pick it up.
            }

            @Override
            public void onLocationUnavailable(String reason) {
                Toast.makeText(OwnerMainActivity.this,
                        "Location access is off. You can enable it later from Settings, or pick your property's location on the map manually.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}