package com.myapplication.office_spaces.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);

        // Already logged in? Skip straight past auth screens.
        if (sessionManager.isLoggedIn()) {
            goToRoleHome(sessionManager.getRole());
            return;
        }

        findViewById(R.id.btnGetStarted).setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void goToRoleHome(String role) {

        Intent intent;

        if ("OWNER".equalsIgnoreCase(role)) {

            intent = new Intent(
                    SplashActivity.this,
                    OwnerMainActivity.class
            );

        } else if ("ADMIN".equalsIgnoreCase(role)) {

            intent = new Intent(
                    SplashActivity.this,
                    AdminMainActivity.class
            );

        }  else if ("RENTER".equalsIgnoreCase(role)
        || "BUYER".equalsIgnoreCase(role)
        || "USER".equalsIgnoreCase(role)) {

        intent = new Intent(
                SplashActivity.this,
                RenterMainActivity.class
        );

        } else {

            // Invalid/unknown saved role - return to login
            intent = new Intent(
                    SplashActivity.this,
                    LoginActivity.class
            );
        }

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}