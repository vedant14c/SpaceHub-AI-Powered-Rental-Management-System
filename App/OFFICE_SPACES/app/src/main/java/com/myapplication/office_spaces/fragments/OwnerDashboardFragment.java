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

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.activities.AddListingActivity;
import com.myapplication.office_spaces.activities.OwnerMainActivity;
import com.myapplication.office_spaces.models.OwnerRequestView;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerDashboardFragment extends Fragment {

    private TextView txtWelcome, txtTotalListings, txtActiveListings, txtTotalInquiries, txtTotalBookings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtWelcome = view.findViewById(R.id.txtWelcome);
        txtTotalListings = view.findViewById(R.id.txtTotalListings);
        txtActiveListings = view.findViewById(R.id.txtActiveListings);
        txtTotalInquiries = view.findViewById(R.id.txtTotalInquiries);
        txtTotalBookings = view.findViewById(R.id.txtTotalBookings);

        SessionManager sessionManager = new SessionManager(requireContext());
        if (sessionManager.getName() != null) {
            txtWelcome.setText(String.format("Welcome, %s!", sessionManager.getName()));
        }

        setupQuickActions(view, sessionManager.getUserId());
        loadListingStats(sessionManager.getUserId());
        loadRequestStats(sessionManager.getUserId());
    }

    private void setupQuickActions(View view, int ownerId) {
        view.findViewById(R.id.actionAddListing).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddListingActivity.class)));

        view.findViewById(R.id.actionManageListings).setOnClickListener(v ->
                ((OwnerMainActivity) requireActivity()).switchTab(R.id.nav_owner_listings));

        view.findViewById(R.id.actionManageAvailability).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Manage Availability coming soon", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.actionViewInquiries).setOnClickListener(v ->
                ((OwnerMainActivity) requireActivity()).switchTab(R.id.nav_owner_inquiries));
    }

    private void loadListingStats(int ownerId) {
        ApiClient.getApiService(requireContext()).getPropertiesByOwner(ownerId)
                .enqueue(new Callback<List<Property>>() {
                    @Override
                    public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                        if (!isAdded() || !response.isSuccessful() || response.body() == null) return;

                        List<Property> properties = response.body();
                        txtTotalListings.setText(String.valueOf(properties.size()));

                        int active = 0;
                        for (Property p : properties) {
                            if (Boolean.TRUE.equals(p.getIsApproved())) active++;
                        }
                        txtActiveListings.setText(String.valueOf(active));
                    }

                    @Override
                    public void onFailure(Call<List<Property>> call, Throwable t) {
                        // Leave stats at their default "0" on failure.
                    }
                });
    }

    private void loadRequestStats(int ownerId) {
        ApiClient.getApiService(requireContext()).getRequestsByOwner(ownerId)
                .enqueue(new Callback<List<OwnerRequestView>>() {
                    @Override
                    public void onResponse(Call<List<OwnerRequestView>> call, Response<List<OwnerRequestView>> response) {
                        if (!isAdded() || !response.isSuccessful() || response.body() == null) return;

                        List<OwnerRequestView> requests = response.body();
                        txtTotalInquiries.setText(String.valueOf(requests.size()));

                        int accepted = 0;
                        for (OwnerRequestView r : requests) {
                            if (r.getStatus() != null && r.getStatus().equalsIgnoreCase("accepted")) accepted++;
                        }
                        txtTotalBookings.setText(String.valueOf(accepted));
                    }

                    @Override
                    public void onFailure(Call<List<OwnerRequestView>> call, Throwable t) {
                        // Leave stats at their default "0" on failure.
                    }
                });
    }
}