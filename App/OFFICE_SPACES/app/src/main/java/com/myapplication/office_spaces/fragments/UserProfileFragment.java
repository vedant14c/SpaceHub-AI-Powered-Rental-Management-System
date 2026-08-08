package com.myapplication.office_spaces.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.activities.EditProfileActivity;
import com.myapplication.office_spaces.activities.FavoritesActivity;
import com.myapplication.office_spaces.activities.LoginActivity;
import com.myapplication.office_spaces.activities.MyRequestsActivity;
import com.myapplication.office_spaces.models.MyProfileView;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileFragment extends Fragment {

    private TextView txtName, txtEmail, txtRole, txtProfileInitial;
    private TextView txtPreferredCity, txtPreferredPropertyType, txtMaxBudget;
    private MaterialButton btnMyRequests, btnFavorites, btnEditProfile, btnLogout;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        initViews(view);
        setupClickListeners();
        
        loadProfile();
    }

    private void initViews(View view) {
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtRole = view.findViewById(R.id.txtRole);
        txtProfileInitial = view.findViewById(R.id.txtProfileInitial);
        
        txtPreferredCity = view.findViewById(R.id.txtPreferredCity);
        txtPreferredPropertyType = view.findViewById(R.id.txtPreferredPropertyType);
        txtMaxBudget = view.findViewById(R.id.txtMaxBudget);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnMyRequests = view.findViewById(R.id.btnMyRequests);
        btnFavorites = view.findViewById(R.id.btnFavorites);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        btnMyRequests.setOnClickListener(v -> startActivity(new Intent(requireContext(), MyRequestsActivity.class)));
        btnFavorites.setOnClickListener(v -> startActivity(new Intent(requireContext(), FavoritesActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadProfile() {
        // First show session data (fast)
        updateBasicUI(sessionManager.getName(), sessionManager.getEmail(), sessionManager.getRole());

        // Then fetch latest from server
        ApiClient.getApiService(requireContext()).getMyProfile().enqueue(new Callback<MyProfileView>() {
            @Override
            public void onResponse(@NonNull Call<MyProfileView> call, @NonNull Response<MyProfileView> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    populateProfile(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyProfileView> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Error refreshing profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateBasicUI(String name, String email, String role) {
        txtName.setText(name != null ? name : "User");
        txtEmail.setText(email);
        txtRole.setText(role);

        if (name != null && !name.isEmpty()) {
            txtProfileInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
    }

    private void populateProfile(MyProfileView profile) {
        updateBasicUI(profile.getName(), profile.getEmail(), profile.getRole());

        txtPreferredCity.setText(String.format("Preferred City: %s", 
                profile.getPreferredCity() != null ? profile.getPreferredCity() : "-"));
        
        txtPreferredPropertyType.setText(String.format("Property Type: %s", 
                profile.getPreferredPropertyType() != null ? profile.getPreferredPropertyType() : "-"));

        if (profile.getMaxBudget() != null && profile.getMaxBudget() > 0) {
            txtMaxBudget.setText(String.format(Locale.getDefault(), "Max Budget: ₹%,.0f", profile.getMaxBudget()));
        } else {
            txtMaxBudget.setText("Max Budget: -");
        }

        // Sync session just in case
        sessionManager.saveSession(
                sessionManager.getUserId(),
                profile.getName(),
                profile.getEmail(),
                profile.getRole(),
                sessionManager.getToken()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
    }

    private void logout() {
        sessionManager.clearSession();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}