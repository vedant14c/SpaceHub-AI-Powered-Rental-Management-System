package com.myapplication.office_spaces.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission denied. You won't receive alerts.", Toast.LENGTH_SHORT).show();
                }
                proceed();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                proceed();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            proceed();
        }
    }

    private void proceed() {
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