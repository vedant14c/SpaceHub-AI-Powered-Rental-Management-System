package com.myapplication.office_spaces.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.fragments.BookingsFragment;
import com.myapplication.office_spaces.fragments.HomeFragment;
import com.myapplication.office_spaces.fragments.InboxFragment;
import com.myapplication.office_spaces.fragments.SearchFragment;
import com.myapplication.office_spaces.fragments.UserProfileFragment;

public class RenterMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    // Values received from Home screen
    private String pendingPropertyType = null;
    private String pendingListingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renter_main);

        bottomNav = findViewById(R.id.bottomNav);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {

                loadFragment(new HomeFragment());
                return true;

            } else if (id == R.id.nav_search) {

                SearchFragment fragment =
                        SearchFragment.newInstance(
                                pendingPropertyType,
                                pendingListingType
                        );

                loadFragment(fragment);

                pendingPropertyType = null;
                pendingListingType = null;

                return true;

            } else if (id == R.id.nav_bookings) {

                loadFragment(new BookingsFragment());
                return true;

            } else if (id == R.id.nav_inbox) {

                loadFragment(new InboxFragment());
                return true;

            } else if (id == R.id.nav_profile) {

                loadFragment(new UserProfileFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /**
     * Called from HomeFragment.
     */
    public void navigateToSearch(String listingType, String propertyType) {

        this.pendingListingType = listingType;
        this.pendingPropertyType = propertyType;

        bottomNav.setSelectedItemId(R.id.nav_search);
    }

    public void navigateToSearch(String filter) {
        navigateToSearch(filter.equalsIgnoreCase("All") ? null : filter, null);
    }
}