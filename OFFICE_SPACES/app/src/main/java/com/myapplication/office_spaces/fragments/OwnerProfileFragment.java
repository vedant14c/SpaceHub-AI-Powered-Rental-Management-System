package com.myapplication.office_spaces.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.activities.LoginActivity;
import com.myapplication.office_spaces.utils.SessionManager;

public class OwnerProfileFragment extends Fragment {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtRole;
    private MaterialButton btnLogout;

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_owner_profile,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtRole = view.findViewById(R.id.txtRole);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadOwnerDetails();

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadOwnerDetails() {

        txtName.setText(sessionManager.getName());
        txtEmail.setText(sessionManager.getEmail());
        txtRole.setText(sessionManager.getRole());
    }

    private void logout() {

        sessionManager.clearSession();

        Intent intent = new Intent(
                requireContext(),
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        requireActivity().finish();
    }
}