package com.myapplication.office_spaces.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.ApprovalAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ListingApprovalsFragment extends Fragment {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private RecyclerView recyclerApprovals;
    private View progressBar;
    private TextView txtEmpty;
    private TabLayout tabLayout;
    private ApprovalAdapter adapter;

    private List<Property> allProperties = new ArrayList<>();
    private int selectedTab = 0; // 0=Pending, 1=Approved, 2=Rejected

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listing_approvals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerApprovals = view.findViewById(R.id.recyclerApprovals);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupRecycler();
        setupTabs();
        loadProperties();
    }

    private void setupRecycler() {

        adapter = new ApprovalAdapter(new ApprovalAdapter.OnApprovalActionListener() {

            @Override
            public void onApprove(Property property) {

                new AlertDialog.Builder(requireContext())
                        .setTitle("Approve Listing")
                        .setMessage("Approve \"" + property.getTitle() + "\" ?")
                        .setPositiveButton("Approve",
                                (dialog, which) ->
                                        setApproval(property, true))
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onReject(Property property) {

                new AlertDialog.Builder(requireContext())
                        .setTitle("Reject Listing")
                        .setMessage("Reject \"" + property.getTitle() + "\" ?")
                        .setPositiveButton("Reject",
                                (dialog, which) ->
                                        setApproval(property, false))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerApprovals.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        recyclerApprovals.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                renderCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void loadProperties() {

        progressBar.setVisibility(View.VISIBLE);
        recyclerApprovals.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.GONE);

        ApiClient.getApiService(requireContext())
                .getAllProperties()
                .enqueue(new Callback<List<Property>>() {

                    @Override
                    public void onResponse(Call<List<Property>> call,
                                           Response<List<Property>> response) {

                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        recyclerApprovals.setVisibility(View.VISIBLE);

                        if (!response.isSuccessful() || response.body() == null) {

                            txtEmpty.setText("Unable to load listings");
                            txtEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        allProperties = response.body();
                        renderCurrentTab();
                    }

                    @Override
                    public void onFailure(Call<List<Property>> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        recyclerApprovals.setVisibility(View.GONE);

                        txtEmpty.setText("Couldn't load listings");
                        txtEmpty.setVisibility(View.VISIBLE);

                        Toast.makeText(requireContext(),
                                "Couldn't reach the server.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderCurrentTab() {

        List<Property> filtered = new ArrayList<>();

        boolean showActions = selectedTab == 0;

        for (Property property : allProperties) {

            String status = property.getApprovalStatus();

            if (status == null)
                status = STATUS_PENDING;

            boolean matches =
                    (selectedTab == 0 && STATUS_PENDING.equalsIgnoreCase(status))
                            ||
                            (selectedTab == 1 && STATUS_APPROVED.equalsIgnoreCase(status))
                            ||
                            (selectedTab == 2 && STATUS_REJECTED.equalsIgnoreCase(status));

            if (matches) {
                filtered.add(property);
            }
        }

        adapter.setData(filtered, showActions);

        if (filtered.isEmpty()) {

            switch (selectedTab) {

                case 0:
                    txtEmpty.setText("No pending listings");
                    break;

                case 1:
                    txtEmpty.setText("No approved listings");
                    break;

                case 2:
                    txtEmpty.setText("No rejected listings");
                    break;
            }

            txtEmpty.setVisibility(View.VISIBLE);

        } else {

            txtEmpty.setVisibility(View.GONE);
        }
    }

    private void setApproval(Property property, boolean approve) {

        Call<Property> call = approve
                ? ApiClient.getApiService(requireContext())
                .approveProperty(property.getPropertyId())
                : ApiClient.getApiService(requireContext())
                .rejectProperty(property.getPropertyId());

        call.enqueue(new Callback<Property>() {

            @Override
            public void onResponse(Call<Property> call,
                                   Response<Property> response) {

                if (!isAdded()) return;

                if (response.isSuccessful()) {

                    Toast.makeText(
                            requireContext(),
                            approve
                                    ? "Listing approved successfully"
                                    : "Listing rejected successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadProperties();

                } else {

                    Toast.makeText(
                            requireContext(),
                            "Couldn't update listing.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Property> call,
                                  Throwable t) {

                if (!isAdded()) return;

                Toast.makeText(
                        requireContext(),
                        "Couldn't reach the server.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}