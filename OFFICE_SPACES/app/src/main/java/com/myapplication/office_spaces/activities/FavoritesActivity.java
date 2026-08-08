package com.myapplication.office_spaces.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapplication.office_spaces.R;
import com.myapplication.office_spaces.adapters.PropertyAdapter;
import com.myapplication.office_spaces.models.Property;
import com.myapplication.office_spaces.network.ApiClient;
import com.myapplication.office_spaces.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerFavorites;
    private View progressBar;
    private View emptyState;
    private PropertyAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        sessionManager = new SessionManager(this);

        setupToolbar();
        initViews();
        setupRecycler();
        loadFavorites();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Favorites");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        recyclerFavorites = findViewById(R.id.recyclerFavorites);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupRecycler() {
        adapter = new PropertyAdapter(new PropertyAdapter.OnPropertyClickListener() {
            @Override
            public void onPropertyClick(Property property) {
                Intent intent = new Intent(FavoritesActivity.this, PropertyDetailsActivity.class);
                intent.putExtra(PropertyDetailsActivity.EXTRA_PROPERTY_ID, property.getPropertyId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Property property, boolean isNowFavorite) {
                sessionManager.setFavorite(property.getPropertyId(), isNowFavorite);
                if (!isNowFavorite) {
                    adapter.removeProperty(property);
                    if (adapter.getItemCount() == 0) {
                        emptyState.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));
        recyclerFavorites.setAdapter(adapter);
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        ApiClient.getApiService(this).getApprovedProperties().enqueue(new Callback<List<Property>>() {
            @Override
            public void onResponse(Call<List<Property>> call, Response<List<Property>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Property> favorites = new ArrayList<>();
                    for (Property p : response.body()) {
                        if (sessionManager.isFavorite(p.getPropertyId())) {
                            p.setFavorite(true);
                            favorites.add(p);
                        }
                    }

                    adapter.setProperties(favorites);
                    emptyState.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(FavoritesActivity.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Property>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoritesActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites(); // Refresh in case something was unfavorited in Details screen
    }
}