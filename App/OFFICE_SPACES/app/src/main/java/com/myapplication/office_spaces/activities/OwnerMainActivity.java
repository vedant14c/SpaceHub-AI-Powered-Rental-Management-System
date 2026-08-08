package com.myapplication.office_spaces.activities;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.fragments.OwnerDashboardFragment;
import com.myapplication.office_spaces.fragments.OwnerInquiriesFragment;
import com.myapplication.office_spaces.fragments.OwnerListingsFragment;
import com.myapplication.office_spaces.fragments.OwnerProfileFragment;

public class OwnerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);

        bottomNav = findViewById(R.id.ownerBottomNav);

        if (savedInstanceState == null) {
            loadFragment(new OwnerDashboardFragment());
        }

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
}