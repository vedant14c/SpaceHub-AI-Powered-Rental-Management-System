package com.myapplication.office_spaces.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.activities.PropertyDetailsActivity;
import com.myapplication.office_spaces.activities.RenterMainActivity;
import com.myapplication.office_spaces.activities.SearchResultActivity;
import com.myapplication.office_spaces.adapters.PropertyAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerProperties;
    private ProgressBar progressBar;
    private TextView txtEmpty, txtUserLocation;
    private PropertyAdapter adapter;
    private SessionManager sessionManager;
    private final List<Property> allProperties = new ArrayList<>();
    private final List<Property> currentFilteredProperties = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation = null;
    private String detectedCity = null;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                sessionManager.setAskedLocationPermission(true);
                Boolean fineLocation = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocation = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocation != null && fineLocation || coarseLocation != null && coarseLocation) {
                    fetchUserLocation();
                } else {
                    txtUserLocation.setText("Location permission denied");
                    Toast.makeText(requireContext(),
                            "Location access is off. Enable it later from Settings to see nearby properties first.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        recyclerProperties = view.findViewById(R.id.recyclerProperties);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);
        txtUserLocation = view.findViewById(R.id.txtUserLocation);
        sessionManager = new SessionManager(requireContext());

        setupRecycler();
        setupClicks(view);
        checkLocationPermission();
        loadApprovedProperties();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            sessionManager.setAskedLocationPermission(true);
            fetchUserLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void fetchUserLocation() {
        try {
            // Step 1: Try getting the last known location (instant)
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    userLocation = location;
                    updateLocationUI(location);
                    sortPropertiesByDistance();
                }
                
                // Step 2: Also request a fresh location in the background to refine
                requestFreshLocation();
            });
        } catch (SecurityException e) {
            txtUserLocation.setText("Location error");
        }
    }

    private void requestFreshLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            userLocation = location;
                            updateLocationUI(location);
                            sortPropertiesByDistance();
                        } else if (userLocation == null) {
                            txtUserLocation.setText("Location not found");
                        }
                    });
        } catch (SecurityException ignored) {}
    }

    private void updateLocationUI(Location location) {
        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                detectedCity = addresses.get(0).getLocality();
                String area = addresses.get(0).getSubLocality();
                txtUserLocation.setText("Near " + (area != null ? area + ", " : "") + (detectedCity != null ? detectedCity : "Your Location"));

                sessionManager.saveLastLocation(location.getLatitude(), location.getLongitude(), detectedCity);

                // Re-sort now that we have a city name
                sortPropertiesByDistance();
            } else {
                txtUserLocation.setText("Location detected");
                sessionManager.saveLastLocation(location.getLatitude(), location.getLongitude(), null);
            }
        } catch (Exception e) {
            txtUserLocation.setText("Location detected");
        }
    }

    private void sortPropertiesByDistance() {
        if (currentFilteredProperties.isEmpty()) return;

        for (Property p : currentFilteredProperties) {
            p.setDistance(calculateDistance(p));
        }

        Collections.sort(currentFilteredProperties, (p1, p2) -> {
            // Rule 1: Check City Match
            if (detectedCity != null) {
                boolean p1Matches = p1.getCity() != null && p1.getCity().equalsIgnoreCase(detectedCity);
                boolean p2Matches = p2.getCity() != null && p2.getCity().equalsIgnoreCase(detectedCity);

                if (p1Matches && !p2Matches) return -1;
                if (!p1Matches && p2Matches) return 1;
            }

            // Rule 2: Sort by Distance within those groups
            return Float.compare(p1.getDistance(), p2.getDistance());
        });

        adapter.setProperties(currentFilteredProperties);
    }

    private float calculateDistance(Property p) {
        if (p.getLatitude() == null || p.getLongitude() == null || userLocation == null) {
            return Float.MAX_VALUE;
        }
        Location propLoc = new Location("");
        propLoc.setLatitude(p.getLatitude());
        propLoc.setLongitude(p.getLongitude());
        return userLocation.distanceTo(propLoc);
    }

    private void mockLocationForCity(Property p) {
        String city = p.getCity() != null ? p.getCity().toLowerCase() : "";
        if (city.contains("mumbai")) {
            p.setLatitude(19.0760); p.setLongitude(72.8777);
        } else if (city.contains("pune")) {
            p.setLatitude(18.5204); p.setLongitude(73.8567);
        } else if (city.contains("hyderabad")) {
            p.setLatitude(17.3850); p.setLongitude(78.4867);
        } else if (city.contains("bengaluru") || city.contains("bangalore")) {
            p.setLatitude(12.9716); p.setLongitude(77.5946);
        } else if (city.contains("delhi")) {
            p.setLatitude(28.6139); p.setLongitude(77.2090);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!currentFilteredProperties.isEmpty()) {
            for (Property p : currentFilteredProperties) {
                p.setFavorite(sessionManager.isFavorite(p.getPropertyId()));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void setupRecycler() {
        adapter = new PropertyAdapter(new PropertyAdapter.OnPropertyClickListener() {
            @Override
            public void onPropertyClick(Property property) {
                Intent intent = new Intent(requireContext(), PropertyDetailsActivity.class);
                intent.putExtra(PropertyDetailsActivity.EXTRA_PROPERTY_ID, property.getPropertyId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Property property, boolean isNowFavorite) {
                sessionManager.setFavorite(property.getPropertyId(), isNowFavorite);
            }
        });
        recyclerProperties.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerProperties.setAdapter(adapter);
    }

    private void setupClicks(View view) {
        EditText etHomeSearch = view.findViewById(R.id.etHomeSearch);
        etHomeSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etHomeSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    Intent intent = new Intent(requireContext(), SearchResultActivity.class);
                    intent.putExtra(SearchFragment.EXTRA_QUERY, query);
                    startActivity(intent);
                }
                return true;
            }
            return false;
        });

        view.findViewById(R.id.searchBar).setOnClickListener(v -> {
            etHomeSearch.requestFocus();
            // Optional: show keyboard manually if it doesn't appear
        });

        view.findViewById(R.id.catAll).setOnClickListener(v -> filterByCategory(null));

        view.findViewById(R.id.catPrivateOffice).setOnClickListener(v -> filterByCategory("Private Office"));

        view.findViewById(R.id.catCoworking).setOnClickListener(v -> filterByCategory("Coworking"));

        view.findViewById(R.id.catMeetingRoom).setOnClickListener(v -> filterByCategory("Meeting Room"));
    }

    private void filterByCategory(String category) {
        currentFilteredProperties.clear();
        if (category == null) {
            currentFilteredProperties.addAll(allProperties);
        } else {
            for (Property p : allProperties) {
                if (category.equalsIgnoreCase(p.getPropertyType())) {
                    currentFilteredProperties.add(p);
                }
            }
        }
        sortPropertiesByDistance();
        txtEmpty.setVisibility(currentFilteredProperties.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadApprovedProperties() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        ApiClient.getApiService(requireContext()).getApprovedProperties().enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    allProperties.clear();
                    for (Property p : response.body()) {
                        p.setFavorite(sessionManager.isFavorite(p.getPropertyId()));
                        
                        // MOCK COORDINATES FOR TESTING (since existing DB might be empty)
                        if (p.getLatitude() == null || p.getLatitude() == 0) {
                            mockLocationForCity(p);
                        }

                        allProperties.add(p);
                    }
                    filterByCategory(null); // Initially show all
                } else {
                    txtEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                txtEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Couldn't load properties.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}