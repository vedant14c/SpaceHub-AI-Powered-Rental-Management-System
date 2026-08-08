package com.myapplication.office_spaces.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.MyProfileView;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPreferredCity, etMaxBudget;
    private AutoCompleteTextView dropdownPropertyType, dropdownListingType;
    private MaterialButton btnUpdate;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        setupDropdowns();
        
        loadProfile();

        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPreferredCity = findViewById(R.id.etPreferredCity);
        etMaxBudget = findViewById(R.id.etMaxBudget);
        dropdownPropertyType = findViewById(R.id.dropdownPropertyType);
        dropdownListingType = findViewById(R.id.dropdownListingType);
        btnUpdate = findViewById(R.id.btnUpdate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDropdowns() {
        String[] propertyTypes = {"All Types", "Private Office", "Coworking", "Meeting Room", "Virtual Office"};
        ArrayAdapter<String> propertyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, propertyTypes);
        dropdownPropertyType.setAdapter(propertyAdapter);

        String[] listingTypes = {"All", "Rent"};
        ArrayAdapter<String> listingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listingTypes);
        dropdownListingType.setAdapter(listingAdapter);
    }

    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApiService(this).getMyProfile().enqueue(new Callback<MyProfileView>() {
            @Override
            public void onResponse(@NonNull Call<MyProfileView> call, @NonNull Response<MyProfileView> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    populateUI(response.body());
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyProfileView> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EditProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(MyProfileView user) {
        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etPhone.setText(user.getPhone());
        etPreferredCity.setText(user.getPreferredCity());
        
        if (user.getMaxBudget() != null) {
            etMaxBudget.setText(String.valueOf(user.getMaxBudget()));
        }

        if (user.getPreferredPropertyType() != null) {
            dropdownPropertyType.setText(user.getPreferredPropertyType(), false);
        }
        
        if (user.getPreferredListingType() != null) {
            dropdownListingType.setText(user.getPreferredListingType(), false);
        }
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String preferredCity = etPreferredCity.getText().toString().trim();
        String propertyType = dropdownPropertyType.getText().toString().trim();
        String listingType = dropdownListingType.getText().toString().trim();
        String budgetStr = etMaxBudget.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        MyProfileView profile = new MyProfileView();
        profile.setName(name);
        profile.setPhone(phone);
        profile.setPreferredCity(preferredCity);
        profile.setPreferredPropertyType(propertyType);
        profile.setPreferredListingType(listingType);
        
        if (!budgetStr.isEmpty()) {
            try {
                profile.setMaxBudget(Double.parseDouble(budgetStr));
            } catch (NumberFormatException ignored) {}
        }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        ApiClient.getApiService(this).updatePreferences(profile).enqueue(new Callback<MyProfileView>() {
            @Override
            public void onResponse(@NonNull Call<MyProfileView> call, @NonNull Response<MyProfileView> response) {
                progressBar.setVisibility(View.GONE);
                btnUpdate.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    MyProfileView updated = response.body();
                    // Update session manager with potentially new name
                    sessionManager.saveSession(
                            sessionManager.getUserId(),
                            name,
                            sessionManager.getEmail(),
                            sessionManager.getRole(),
                            sessionManager.getToken()
                    );
                    sessionManager.savePreferences(
                            updated.getPreferredCity(),
                            updated.getPreferredPropertyType(),
                            updated.getPreferredListingType(),
                            updated.getMaxBudget()
                    );
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Update failed (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyProfileView> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnUpdate.setEnabled(true);
                Toast.makeText(EditProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}