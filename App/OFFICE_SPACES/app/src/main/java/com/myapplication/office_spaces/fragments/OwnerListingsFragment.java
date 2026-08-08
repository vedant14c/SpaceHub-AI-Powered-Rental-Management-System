package com.myapplication.office_spaces.fragments;


import android.app.AlertDialog;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.activities.AddListingActivity;
import com.myapplication.office_spaces.activities.PropertyDetailsActivity;
import com.myapplication.office_spaces.adapters.OwnerListingAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerListingsFragment extends Fragment {

    private RecyclerView recyclerListings;
    private View progressBar;
    private TextView txtEmpty;
    private TabLayout tabLayout;
    private OwnerListingAdapter adapter;

    private List<Property> allProperties = new ArrayList<>();
    private OwnerListingAdapter.ListingBucket selectedBucket = OwnerListingAdapter.ListingBucket.ACTIVE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_listings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerListings = view.findViewById(R.id.recyclerListings);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupRecycler();
        setupTabs();

        view.findViewById(R.id.btnAddListing).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AddListingActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadListings(); // refresh in case a listing was just added/edited/deleted
    }

    private void setupRecycler() {
        adapter = new OwnerListingAdapter(new OwnerListingAdapter.OnListingActionListener() {
            @Override
            public void onListingClick(Property property) {
                Intent intent = new Intent(requireContext(), PropertyDetailsActivity.class);
                intent.putExtra(PropertyDetailsActivity.EXTRA_PROPERTY_ID, property.getPropertyId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Property property) {

                Intent intent = new Intent(requireContext(), AddListingActivity.class);
                intent.putExtra("propertyId", property.getPropertyId());

                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Property property) {
                confirmDelete(property);
            }
        });
        recyclerListings.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerListings.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: selectedBucket = OwnerListingAdapter.ListingBucket.ACTIVE; break;
                    case 1: selectedBucket = OwnerListingAdapter.ListingBucket.PENDING; break;
                    case 2: selectedBucket = OwnerListingAdapter.ListingBucket.INACTIVE; break;
                }
                renderCurrentTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void loadListings() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        int ownerId = new SessionManager(requireContext()).getUserId();

        ApiClient.getApiService(requireContext()).getPropertiesByOwner(ownerId)
                .enqueue(new Callback<List<Property>>() {
                    @Override
                    public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);

                        if (!response.isSuccessful() || response.body() == null) {
                            txtEmpty.setVisibility(View.VISIBLE);
                            return;
                        }

                        allProperties = response.body();
                        renderCurrentTab();
                    }

                    @Override
                    public void onFailure(Call<List<Property>> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        txtEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Couldn't load listings. Check your connection.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderCurrentTab() {
        List<Property> filtered = new ArrayList<>();
        for (Property p : allProperties) {
            if (OwnerListingAdapter.classify(p) == selectedBucket) {
                filtered.add(p);
            }
        }

        adapter.setProperties(filtered);
        txtEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(Property property) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete listing?")
                .setMessage("Remove \"" + property.getTitle() + "\" permanently? This can't be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteListing(property))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteListing(Property property) {

        ApiClient.getApiService(requireContext())
                .deleteProperty(property.getPropertyId())
                .enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(
                            Call<String> call,
                            Response<String> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Listing deleted successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadListings();

                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Failed to delete listing",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<String> call,
                            Throwable t) {

                        if (!isAdded()) return;

                        Toast.makeText(
                                requireContext(),
                                "Delete failed: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}