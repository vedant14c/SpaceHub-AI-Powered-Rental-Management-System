package com.myapplication.office_spaces.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.PropertyAdminAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminListingsFragment extends Fragment {

    private RecyclerView recyclerProperties;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    private SearchView searchProperties;

    private Chip chipAll;
    private Chip chipPending;
    private Chip chipApproved;
    private Chip chipRejected;

    private PropertyAdminAdapter adapter;

    private List<Property> allProperties = new ArrayList<>();

    private String currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_admin_listings,
                container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        recyclerProperties = view.findViewById(R.id.recyclerProperties);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        searchProperties = view.findViewById(R.id.searchProperties);

        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipApproved = view.findViewById(R.id.chipApproved);
        chipRejected = view.findViewById(R.id.chipRejected);

        adapter = new PropertyAdminAdapter(new PropertyAdminAdapter.OnPropertyActionListener() {
            @Override
            public void onApprove(Property property) {
                confirmApprove(property);
            }

            @Override
            public void onReject(Property property) {
                confirmReject(property);
            }
        });

        recyclerProperties.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        recyclerProperties.setAdapter(adapter);

        setupSearch();

        setupChips();

        loadProperties();
    }

    private void loadProperties() {

        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService(requireContext())
                .getAllProperties()
                .enqueue(new Callback<List<Property>>() {

                    @Override
                    public void onResponse(Call<List<Property>> call,
                                           Response<List<Property>> response) {

                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() &&
                                response.body() != null) {

                            allProperties = response.body();

                            filterProperties(
                                    searchProperties.getQuery().toString());

                        } else {

                            Toast.makeText(getContext(),
                                    "Unable to load properties",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Property>> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(getContext(),
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSearch() {

        searchProperties.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterProperties(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterProperties(newText);
                        return true;
                    }
                });
    }

    private void setupChips() {

        chipAll.setOnClickListener(v -> {
            currentFilter = "ALL";
            filterProperties(searchProperties.getQuery().toString());
        });

        chipPending.setOnClickListener(v -> {
            currentFilter = "PENDING";
            filterProperties(searchProperties.getQuery().toString());
        });

        chipApproved.setOnClickListener(v -> {
            currentFilter = "APPROVED";
            filterProperties(searchProperties.getQuery().toString());
        });

        chipRejected.setOnClickListener(v -> {
            currentFilter = "REJECTED";
            filterProperties(searchProperties.getQuery().toString());
        });
    }

    private void filterProperties(String keyword) {

        if (keyword == null)
            keyword = "";

        keyword = keyword.toLowerCase().trim();

        List<Property> filtered = new ArrayList<>();

        for (Property property : allProperties) {

            boolean matchesSearch =
                    (property.getTitle() != null &&
                            property.getTitle().toLowerCase().contains(keyword))
                            ||
                            (property.getCity() != null &&
                                    property.getCity().toLowerCase().contains(keyword));

            String status = property.getApprovalStatus();

            if (status == null)
                status = "PENDING";

            boolean matchesStatus =
                    currentFilter.equals("ALL")
                            ||
                            status.equalsIgnoreCase(currentFilter);

            if (matchesSearch && matchesStatus) {
                filtered.add(property);
            }
        }

        adapter.setProperties(filtered);

        txtEmpty.setVisibility(
                filtered.isEmpty()
                        ? View.VISIBLE
                        : View.GONE);
    }

    private void confirmApprove(Property property) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Approve Property")
                .setMessage("Approve " + property.getTitle() + "?")
                .setPositiveButton("Approve",
                        (dialog, which) ->
                                approveProperty(property.getPropertyId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmReject(Property property) {

        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Property")
                .setMessage("Reject " + property.getTitle() + "?")
                .setPositiveButton("Reject",
                        (dialog, which) ->
                                rejectProperty(property.getPropertyId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveProperty(int id) {

        ApiClient.getApiService(requireContext())
                .approveProperty(id)
                .enqueue(new Callback<Property>() {

                    @Override
                    public void onResponse(Call<Property> call,
                                           Response<Property> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {

                            Toast.makeText(getContext(),
                                    "Property Approved",
                                    Toast.LENGTH_SHORT).show();

                            loadProperties();

                        } else {

                            Toast.makeText(getContext(),
                                    "Approval failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Property> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(getContext(),
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rejectProperty(int id) {

        ApiClient.getApiService(requireContext())
                .rejectProperty(id)
                .enqueue(new Callback<Property>() {

                    @Override
                    public void onResponse(Call<Property> call,
                                           Response<Property> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {

                            Toast.makeText(getContext(),
                                    "Property Rejected",
                                    Toast.LENGTH_SHORT).show();

                            loadProperties();

                        } else {

                            Toast.makeText(getContext(),
                                    "Rejection failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Property> call,
                                          Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(getContext(),
                                "Server connection failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}