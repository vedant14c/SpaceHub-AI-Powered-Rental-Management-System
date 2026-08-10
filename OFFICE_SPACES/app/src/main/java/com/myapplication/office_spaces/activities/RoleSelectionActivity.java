package com.myapplication.office_spaces.activities;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.myapplication.office_spaces.R;


public class RoleSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_ROLE = "selected_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        findViewById(R.id.cardOwner).setOnClickListener(v -> goToRegister("OWNER"));
        findViewById(R.id.cardRenter).setOnClickListener(v -> goToRegister("USER"));
    }

    private void goToRegister(String role) {
        Intent intent = new Intent(RoleSelectionActivity.this, RegisterActivity.class);
        intent.putExtra(EXTRA_ROLE, role);
        startActivity(intent);
    }
}