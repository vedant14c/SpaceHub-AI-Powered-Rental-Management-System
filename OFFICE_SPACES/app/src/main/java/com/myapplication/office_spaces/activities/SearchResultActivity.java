package com.myapplication.office_spaces.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.PropertyAdapter;
import com.myapplication.office_spaces.fragments.SearchFragment;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The backend has no server-side filter endpoint (only /properties/approved,
 * /properties/{id}, /properties/owner/{ownerId}), so this screen fetches all
 * approved properties and applies the filters chosen on the Search screen
 * client-side. Swap loadAndFilterProperties() to call a real filter endpoint
 * if you add one later.
 */
public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView recyclerResults;
    private View progressBar;
    private TextView txtEmpty;
    private TextView txtResultsCount;
    private PropertyAdapter adapter;
    private SessionManager sessionManager;
    private final List<Property> filteredProperties = new ArrayList<>();

    private String query, city, propertyType, listingType;
    private float minPrice, maxPrice, minArea, maxArea;
    private float radiusKm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        readFilters();
        initViews();
        setupRecycler();
        setupClicks();
        loadAndFilterProperties();
    }

    private void readFilters() {
        Intent intent = getIntent();
        query = intent.getStringExtra(SearchFragment.EXTRA_QUERY);
        city = intent.getStringExtra(SearchFragment.EXTRA_CITY);
        propertyType = intent.getStringExtra(SearchFragment.EXTRA_PROPERTY_TYPE);
        listingType = intent.getStringExtra(SearchFragment.EXTRA_LISTING_TYPE);
        minPrice = intent.getFloatExtra(SearchFragment.EXTRA_MIN_PRICE, 0f);
        maxPrice = intent.getFloatExtra(SearchFragment.EXTRA_MAX_PRICE, Float.MAX_VALUE);
        minArea = intent.getFloatExtra(SearchFragment.EXTRA_MIN_AREA, 0f);
        maxArea = intent.getFloatExtra(SearchFragment.EXTRA_MAX_AREA, Float.MAX_VALUE);
        radiusKm = intent.getFloatExtra(SearchFragment.EXTRA_RADIUS_KM, -1f);
    }

    private void initViews() {
        recyclerResults = findViewById(R.id.recyclerResults);
        progressBar = findViewById(R.id.progressBar);
        txtEmpty = findViewById(R.id.txtEmpty);
        txtResultsCount = findViewById(R.id.txtResultsCount);
        sessionManager = new SessionManager(this);
    }

    private void setupRecycler() {
        adapter = new PropertyAdapter(new PropertyAdapter.OnPropertyClickListener() {
            @Override
            public void onPropertyClick(Property property) {
                Intent intent = new Intent(SearchResultActivity.this, PropertyDetailsActivity.class);
                intent.putExtra(PropertyDetailsActivity.EXTRA_PROPERTY_ID, property.getPropertyId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Property property, boolean isNowFavorite) {
                sessionManager.setFavorite(property.getPropertyId(), isNowFavorite);
            }
        });

        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(adapter);
    }

    private void setupClicks() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFilter).setOnClickListener(v -> finish()); // back to Search tab to adjust filters
    }

    private void loadAndFilterProperties() {
        runManualFilterSearch();
    }

    private void runManualFilterSearch() {
        progressBar.setVisibility(View.VISIBLE);
        txtEmpty.setVisibility(View.GONE);

        ApiClient.getApiService(this).getApprovedProperties().enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    txtEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                filteredProperties.clear();
                filteredProperties.addAll(applyFilters(response.body()));
                applyRadiusFilterAndSort();

                for (Property p : filteredProperties) {
                    p.setFavorite(sessionManager.isFavorite(p.getPropertyId()));
                }

                txtResultsCount.setText(String.format(Locale.getDefault(), "%d Results Found", filteredProperties.size()));

                if (filteredProperties.isEmpty()) {
                    txtEmpty.setVisibility(View.VISIBLE);
                } else {
                    adapter.setProperties(filteredProperties);
                }
            }

            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                txtEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(SearchResultActivity.this,
                        "Couldn't load results. Check your connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!filteredProperties.isEmpty()) {
            for (Property p : filteredProperties) {
                p.setFavorite(sessionManager.isFavorite(p.getPropertyId()));
            }
            adapter.notifyDataSetChanged();
        }
    }

    private List<Property> applyFilters(List<Property> all) {
        List<Property> result = new ArrayList<>();

        for (Property p : all) {
            if (!matchesQuery(p)) continue;
            if (!matchesCity(p)) continue;
            if (!matchesPropertyType(p)) continue;
            if (!matchesListingType(p)) continue;
            if (!matchesPrice(p)) continue;
            if (!matchesArea(p)) continue;
            result.add(p);
        }
        return result;
    }

    private boolean matchesListingType(Property p) {
        if (listingType == null || listingType.isEmpty() || listingType.equalsIgnoreCase("All")) return true;
        return containsIgnoreCase(p.getListingType(), listingType);
    }

    private boolean matchesQuery(Property p) {
        if (query == null || query.isEmpty()) return true;

        String[] keywords = query.toLowerCase(Locale.getDefault()).split("\\s+");

        for (String word : keywords) {
            boolean wordMatches = containsIgnoreCase(p.getTitle(), word)
                    || containsIgnoreCase(p.getCity(), word)
                    || containsIgnoreCase(p.getAddress(), word)
                    || containsIgnoreCase(p.getDescription(), word)
                    || containsIgnoreCase(p.getPropertyType(), word);

            if (!wordMatches) return false;
        }
        return true;
    }

    private boolean matchesCity(Property p) {
        if (city == null || city.isEmpty()) return true;
        String cityOnly = city.split(",")[0].trim();
        return containsIgnoreCase(p.getCity(), cityOnly);
    }

    private boolean matchesPropertyType(Property p) {
        if (propertyType == null || propertyType.isEmpty() || Objects.equals(propertyType, "All Types")) return true;
        return containsIgnoreCase(p.getPropertyType(), propertyType);
    }

    private boolean matchesPrice(Property p) {
        if (p.getPrice() == null) return true;
        if (maxPrice >= 500000) { // Slider max value from fragment_search.xml
            return p.getPrice() >= minPrice;
        }
        return p.getPrice() >= minPrice && p.getPrice() <= maxPrice;
    }

    private boolean matchesArea(Property p) {
        if (p.getAreaSqft() == null) return true;
        if (maxArea >= 10000) { // Slider max value from fragment_search.xml
            return p.getAreaSqft() >= minArea;
        }
        return p.getAreaSqft() >= minArea && p.getAreaSqft() <= maxArea;
    }

    private boolean containsIgnoreCase(String source, String target) {
        if (source == null) return false;
        return source.toLowerCase(Locale.getDefault()).contains(target.toLowerCase(Locale.getDefault()));
    }

    /**
     * If the user picked a "Within X km" radius on the Search screen, filters the results
     * down to properties within that distance of their last-known location and sorts them
     * nearest-first. Uses SessionManager's cached GPS fix (populated by HomeFragment /
     * OwnerMainActivity's location permission flow) rather than requesting a fresh GPS fix
     * here, keeping this screen simple and fast.
     */
    private void applyRadiusFilterAndSort() {
        if (radiusKm <= 0) return; // "Any distance" selected - no radius filtering.

        if (!sessionManager.hasLastLocation()) {
            Toast.makeText(this,
                    "Turn on location access from the Home tab to filter by distance.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        double userLat = sessionManager.getLastLatitude();
        double userLng = sessionManager.getLastLongitude();

        List<Property> withinRadius = new ArrayList<>();
        for (Property p : filteredProperties) {
            if (p.getLatitude() == null || p.getLongitude() == null) continue;

            float[] results = new float[1];
            android.location.Location.distanceBetween(userLat, userLng, p.getLatitude(), p.getLongitude(), results);
            float km = results[0] / 1000f;
            p.setDistance(results[0]); // metres, consumed by PropertyAdapter's "X km away" label

            if (km <= radiusKm) {
                withinRadius.add(p);
            }
        }

        withinRadius.sort((p1, p2) -> Float.compare(p1.getDistance(), p2.getDistance()));

        filteredProperties.clear();
        filteredProperties.addAll(withinRadius);
    }
}