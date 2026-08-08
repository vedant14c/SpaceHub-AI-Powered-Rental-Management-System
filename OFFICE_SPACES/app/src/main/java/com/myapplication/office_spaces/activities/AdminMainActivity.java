package com.myapplication.office_spaces.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.fragments.AdminDashboardFragment;
import com.myapplication.office_spaces.fragments.ListingApprovalsFragment;
import com.myapplication.office_spaces.fragments.ManageRequestsFragment;
import com.myapplication.office_spaces.fragments.ManageUsersFragment;

public class AdminMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        bottomNav = findViewById(R.id.adminBottomNav);

        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment());
            bottomNav.setSelectedItemId(R.id.nav_admin_dashboard);
        }

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_admin_dashboard) {

                loadFragment(new AdminDashboardFragment());
                return true;

            } else if (id == R.id.nav_admin_users) {

                loadFragment(new ManageUsersFragment());
                return true;

            } else if (id == R.id.nav_admin_approvals) {

                loadFragment(new ListingApprovalsFragment());
                return true;

            } else if (id == R.id.nav_admin_requests) {

                loadFragment(new ManageRequestsFragment());
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment)
                .commit();
    }
}