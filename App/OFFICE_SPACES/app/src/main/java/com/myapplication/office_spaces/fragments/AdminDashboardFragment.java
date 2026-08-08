package com.myapplication.office_spaces.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.models.PropertyRequest;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.models.AdminDashboard;
import android.content.Intent;
import android.widget.Button;
import com.myapplication.office_spaces.activities.LoginActivity;
import com.myapplication.office_spaces.utils.SessionManager;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private TextView txtTotalUsers, txtOwnerCount, txtRenterCount;
    private TextView txtTotalListings, txtPendingApprovals, txtTotalBookings;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTotalUsers = view.findViewById(R.id.txtTotalUsers);
        txtOwnerCount = view.findViewById(R.id.txtOwnerCount);
        txtRenterCount = view.findViewById(R.id.txtRenterCount);
        txtTotalListings = view.findViewById(R.id.txtTotalListings);
        txtPendingApprovals = view.findViewById(R.id.txtPendingApprovals);
        txtTotalBookings = view.findViewById(R.id.txtTotalBookings);
        btnLogout = view.findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            SessionManager sessionManager = new SessionManager(requireContext());
            sessionManager.clearSession();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            requireActivity().finish();
        });

        loadDashboardStats();
        loadRealBookingStats();
    }

    private void loadDashboardStats() {

        ApiClient.getApiService(requireContext())
                .getDashboardStats()
                .enqueue(new Callback<AdminDashboard>() {

                    @Override
                    public void onResponse(Call<AdminDashboard> call,
                                           Response<AdminDashboard> response) {

                        if (!isAdded() || !response.isSuccessful() || response.body() == null)
                            return;

                        AdminDashboard dashboard = response.body();

                        txtTotalUsers.setText(String.valueOf(dashboard.getTotalUsers()));
                        txtOwnerCount.setText(String.valueOf(dashboard.getPropertyOwners()));
                        txtRenterCount.setText(String.valueOf(dashboard.getRentersBuyers()));

                        txtTotalListings.setText(String.valueOf(dashboard.getTotalListings()));
                        txtPendingApprovals.setText(String.valueOf(dashboard.getPendingListings()));
                    }

                    @Override
                    public void onFailure(Call<AdminDashboard> call, Throwable t) {
                    }
                });
    }



    private void loadRealBookingStats() {
        ApiClient.getApiService(requireContext()).getAllRequests(null).enqueue(new Callback<List<PropertyRequest>>() {
            @Override
            public void onResponse(Call<List<PropertyRequest>> call, Response<List<PropertyRequest>> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;

                int accepted = 0;
                for (PropertyRequest r : response.body()) {
                    if (r.getStatus() != null && r.getStatus().equalsIgnoreCase("accepted")) accepted++;
                }
                txtTotalBookings.setText(String.valueOf(accepted));
            }

            @Override
            public void onFailure(Call<List<PropertyRequest>> call, Throwable t) {
            }
        });
    }
}